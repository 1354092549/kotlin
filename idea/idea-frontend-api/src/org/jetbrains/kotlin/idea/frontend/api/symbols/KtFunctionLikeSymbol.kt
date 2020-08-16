/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.frontend.api.symbols

import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.KtSymbolWithKind
import org.jetbrains.kotlin.idea.frontend.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

sealed class KtFunctionLikeSymbol : KtCallableSymbol(), KtTypedSymbol, KtSymbolWithKind {
    abstract val valueParameters: List<KtParameterSymbol>

    abstract override fun createPointer(): KtSymbolPointer<KtFunctionLikeSymbol>
}

abstract class KtAnonymousFunctionSymbol : KtFunctionLikeSymbol() {
    final override val symbolKind: KtSymbolKind get() = KtSymbolKind.LOCAL
    final override val containingNonLocalClassIdIfMember: ClassId? get() = null
    final override val containingPackageFqNameIfTopLevel: FqName? get() = null

    abstract override fun createPointer(): KtSymbolPointer<KtAnonymousFunctionSymbol>
}

abstract class KtFunctionSymbol : KtFunctionLikeSymbol(),
    KtNamedSymbol,
    KtPossibleExtensionSymbol,
    KtSymbolWithTypeParameters,
    KtSymbolWithModality<KtCommonSymbolModality> {
    abstract val fqNameIfNonLocal: FqName?

    abstract val isSuspend: Boolean
    abstract val isOperator: Boolean

    abstract override val valueParameters: List<KtFunctionParameterSymbol>

    abstract override fun createPointer(): KtSymbolPointer<KtFunctionSymbol>
}

abstract class KtConstructorSymbol : KtFunctionLikeSymbol() {
    abstract val isPrimary: Boolean

    abstract override val valueParameters: List<KtConstructorParameterSymbol>

    final override val symbolKind: KtSymbolKind get() = KtSymbolKind.MEMBER
    final override val containingPackageFqNameIfTopLevel: FqName? get() = null

    abstract override fun createPointer(): KtSymbolPointer<KtConstructorSymbol>
}