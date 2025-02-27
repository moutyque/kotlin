/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.light.classes.symbol.classes

import com.intellij.psi.*
import com.intellij.psi.impl.InheritanceImplUtil
import com.intellij.psi.impl.PsiClassImplUtil
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import org.jetbrains.annotations.NonNls
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.asJava.classes.getParentForLocalDeclaration
import org.jetbrains.kotlin.asJava.classes.lazyPub
import org.jetbrains.kotlin.asJava.elements.KtLightField
import org.jetbrains.kotlin.asJava.elements.KtLightIdentifier
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.light.classes.symbol.*
import org.jetbrains.kotlin.light.classes.symbol.annotations.hasDeprecatedAnnotation
import org.jetbrains.kotlin.light.classes.symbol.parameters.SymbolLightTypeParameterList
import org.jetbrains.kotlin.load.java.structure.LightClassOriginKind
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.debugText.getDebugText
import org.jetbrains.kotlin.psi.stubs.KotlinClassOrObjectStub
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

abstract class SymbolLightClassForClassLike<SType : KtClassOrObjectSymbol> protected constructor(
    internal val classOrObjectDeclaration: KtClassOrObject?,
    internal val classOrObjectSymbolPointer: KtSymbolPointer<SType>,
    ktModule: KtModule,
    manager: PsiManager,
) : SymbolLightClassBase(ktModule, manager),
    StubBasedPsiElement<KotlinClassOrObjectStub<out KtClassOrObject>> {
    constructor(
        ktAnalysisSession: KtAnalysisSession,
        ktModule: KtModule,
        classOrObjectSymbol: SType,
        manager: PsiManager,
    ) : this(
        classOrObjectDeclaration = classOrObjectSymbol.sourcePsiSafe(),
        classOrObjectSymbolPointer = with(ktAnalysisSession) {
            @Suppress("UNCHECKED_CAST")
            classOrObjectSymbol.createPointer() as KtSymbolPointer<SType>
        },
        ktModule = ktModule,
        manager = manager,
    )

    override val kotlinOrigin: KtClassOrObject? get() = classOrObjectDeclaration

    internal inline fun <T> withClassOrObjectSymbol(crossinline action: KtAnalysisSession.(SType) -> T): T =
        classOrObjectSymbolPointer.withSymbol(ktModule, action)

    override val isTopLevel: Boolean by lazyPub {
        classOrObjectDeclaration?.isTopLevel() ?: withClassOrObjectSymbol { it.symbolKind == KtSymbolKind.TOP_LEVEL }
    }

    internal val isCompanionObject: Boolean by lazyPub {
        classOrObjectDeclaration?.let { it is KtObjectDeclaration && it.isCompanion() } ?: withClassOrObjectSymbol {
            it.classKind == KtClassKind.COMPANION_OBJECT
        }
    }

    internal val isLocal: Boolean by lazyPub {
        classOrObjectDeclaration?.isLocal ?: withClassOrObjectSymbol { it.symbolKind == KtSymbolKind.LOCAL }
    }

    internal val isNamedObject: Boolean by lazyPub {
        classOrObjectDeclaration?.let { it is KtObjectDeclaration && !it.isCompanion() } ?: withClassOrObjectSymbol {
            it.classKind == KtClassKind.OBJECT
        }
    }

    internal val isObject: Boolean by lazyPub {
        classOrObjectDeclaration?.let { it is KtObjectDeclaration } ?: withClassOrObjectSymbol { it.classKind.isObject }
    }

    internal val isInterface: Boolean by lazyPub {
        classOrObjectDeclaration?.let { it is KtClass && it.isInterface() } ?: withClassOrObjectSymbol {
            it.classKind == KtClassKind.INTERFACE
        }
    }

    internal val isAnnotation: Boolean by lazyPub {
        classOrObjectDeclaration?.let { it is KtClass && it.isAnnotation() } ?: withClassOrObjectSymbol {
            it.classKind == KtClassKind.ANNOTATION_CLASS
        }
    }

    internal val isEnum: Boolean by lazyPub {
        classOrObjectDeclaration?.let { it is KtClass && it.isEnum() } ?: withClassOrObjectSymbol {
            it.classKind == KtClassKind.ENUM_CLASS
        }
    }

    private val _isDeprecated: Boolean by lazyPub {
        withClassOrObjectSymbol { it.hasDeprecatedAnnotation() }
    }

    override fun isDeprecated(): Boolean = _isDeprecated

    abstract override fun getModifierList(): PsiModifierList?
    abstract override fun getOwnFields(): List<KtLightField>
    abstract override fun getOwnMethods(): List<PsiMethod>

    private val _identifier: PsiIdentifier by lazyPub {
        KtLightIdentifier(this, classOrObjectDeclaration)
    }

    override fun getNameIdentifier(): PsiIdentifier? = _identifier

    abstract override fun getExtendsList(): PsiReferenceList?
    abstract override fun getImplementsList(): PsiReferenceList?

    private val _typeParameterList: PsiTypeParameterList? by lazyPub {
        hasTypeParameters().ifTrue {
            SymbolLightTypeParameterList(
                owner = this,
                symbolWithTypeParameterPointer = classOrObjectSymbolPointer,
                ktModule = ktModule,
                ktDeclaration = classOrObjectDeclaration,
            )
        }
    }

    override fun hasTypeParameters(): Boolean = hasTypeParameters(ktModule, classOrObjectDeclaration, classOrObjectSymbolPointer)

    override fun getTypeParameterList(): PsiTypeParameterList? = _typeParameterList
    override fun getTypeParameters(): Array<PsiTypeParameter> = _typeParameterList?.typeParameters ?: PsiTypeParameter.EMPTY_ARRAY

    private val _ownInnerClasses: List<SymbolLightClassBase> by lazyPub {
        withClassOrObjectSymbol {
            it.createInnerClasses(manager, this@SymbolLightClassForClassLike, classOrObjectDeclaration)
        }
    }

    override fun getOwnInnerClasses(): List<PsiClass> = _ownInnerClasses

    override fun getTextOffset(): Int = classOrObjectDeclaration?.textOffset ?: -1
    override fun getStartOffsetInParent(): Int = classOrObjectDeclaration?.startOffsetInParent ?: -1
    override fun isWritable() = false

    override fun getNavigationElement(): PsiElement = classOrObjectDeclaration ?: this

    override fun isEquivalentTo(another: PsiElement?): Boolean =
        isEquivalentToByName(another) ||
                isOriginEquivalentTo(another)

    protected fun isEquivalentToByName(another: PsiElement?): Boolean = basicIsEquivalentTo(this, another) ||
            another is PsiClass && qualifiedName != null && another.qualifiedName == qualifiedName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SymbolLightClassForClassLike<*> || other.ktModule != ktModule || other.manager != manager) return false
        if (classOrObjectDeclaration != null || other.classOrObjectDeclaration != null) {
            return other.classOrObjectDeclaration == classOrObjectDeclaration
        }

        return compareSymbolPointers(classOrObjectSymbolPointer, other.classOrObjectSymbolPointer)
    }

    override fun hashCode(): Int = classOrObjectDeclaration.hashCode()

    override fun getName(): String? = classOrObjectDeclaration?.name ?: withClassOrObjectSymbol {
        it.name?.asString()
    }

    override fun hasModifierProperty(@NonNls name: String): Boolean = modifierList?.hasModifierProperty(name) ?: false

    override fun isInterface(): Boolean = isInterface
    override fun isAnnotationType(): Boolean = isAnnotation
    override fun isEnum(): Boolean = isEnum

    override fun isValid(): Boolean = classOrObjectDeclaration?.isValid ?: classOrObjectSymbolPointer.isValid(ktModule)

    override fun toString() = "${this::class.java.simpleName}:${classOrObjectDeclaration?.getDebugText()}"

    override fun getUseScope(): SearchScope = classOrObjectDeclaration?.useScope ?: GlobalSearchScope.projectScope(project)
    override fun getElementType(): IStubElementType<out StubElement<*>, *>? = classOrObjectDeclaration?.elementType
    override fun getStub(): KotlinClassOrObjectStub<out KtClassOrObject>? = classOrObjectDeclaration?.stub

    override val originKind: LightClassOriginKind get() = LightClassOriginKind.SOURCE

    override fun getQualifiedName(): String? = classOrObjectDeclaration?.fqName?.asString()

    override fun getInterfaces(): Array<PsiClass> = PsiClassImplUtil.getInterfaces(this)
    override fun getSuperClass(): PsiClass? = PsiClassImplUtil.getSuperClass(this)
    override fun getSupers(): Array<PsiClass> = PsiClassImplUtil.getSupers(this)
    override fun getSuperTypes(): Array<PsiClassType> = PsiClassImplUtil.getSuperTypes(this)

    override fun getContainingClass(): PsiClass? {
        val containingBody = classOrObjectDeclaration?.parent as? KtClassBody
        val containingClass = containingBody?.parent as? KtClassOrObject
        containingClass?.let { return it.toLightClass() }
        return null
    }

    override fun getParent(): PsiElement? {
        if (isLocal) {
            return classOrObjectDeclaration?.let(::getParentForLocalDeclaration)
        }

        return containingClass ?: containingFile
    }

    override fun getScope(): PsiElement? = parent

    override fun isInheritorDeep(baseClass: PsiClass?, classToByPass: PsiClass?): Boolean =
        baseClass?.let { InheritanceImplUtil.isInheritorDeep(this, it, classToByPass) } ?: false

    abstract override fun copy(): SymbolLightClassForClassLike<*>
}
