/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.common

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.SERIALIZER_PROVIDER_NAME
import org.jetbrains.kotlinx.serialization.compiler.resolve.getSerializableClassDescriptorByCompanion
import org.jetbrains.kotlinx.serialization.compiler.resolve.isKSerializer
import org.jetbrains.kotlinx.serialization.compiler.resolve.isSerializableObject

abstract class SerializableCompanionCodegen(
    protected val companionDescriptor: ClassDescriptor,
    bindingContext: BindingContext
) : AbstractSerialGenerator(bindingContext, companionDescriptor) {
    protected val serializableDescriptor: ClassDescriptor = getSerializableClassDescriptorByCompanion(companionDescriptor)!!

    fun generate() {
        val serializerGetterDescriptor = findSerializerGetterOnCompanion(serializableDescriptor) ?: throw IllegalStateException(
            "Can't find synthesized 'Companion.serializer()' function to generate, " +
                    "probably clash with user-defined function has occurred"
        )
        generateSerializerGetter(serializerGetterDescriptor)
    }

    protected abstract fun generateSerializerGetter(methodDescriptor: FunctionDescriptor)

    companion object {
        fun findSerializerGetterOnCompanion(serializableDescriptor: ClassDescriptor): FunctionDescriptor? {
            val companionObjectDesc = if (serializableDescriptor.isSerializableObject) serializableDescriptor else serializableDescriptor.companionObjectDescriptor
            return companionObjectDesc?.unsubstitutedMemberScope?.getContributedFunctions(
                SERIALIZER_PROVIDER_NAME,
                NoLookupLocation.FROM_BACKEND
            )?.firstOrNull {
                it.valueParameters.size == serializableDescriptor.declaredTypeParameters.size
                        && it.kind == CallableMemberDescriptor.Kind.SYNTHESIZED
                        && it.valueParameters.all { p -> isKSerializer(p.type) }
                        && it.returnType != null && isKSerializer(it.returnType)
            }
        }
    }
}