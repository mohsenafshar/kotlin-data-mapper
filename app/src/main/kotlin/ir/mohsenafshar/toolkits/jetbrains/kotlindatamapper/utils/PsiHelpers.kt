package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiUtil
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.sourcePsi
import org.jetbrains.kotlin.analysis.api.types.KaClassType
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.asJava.elements.KtLightFieldForSourceDeclarationSupport
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtParameter

/**
 * Checks if the receiver is a Kotlin data class.
 *
 * @receiver A [PsiElement] of type [KtLightClassForSourceDeclaration].
 * @return `true` if the receiver is a Kotlin data class; otherwise, `false`.
 */
fun PsiElement?.isKotlinDataClass(): Boolean {
    this ?: return false

    if (this is KtLightClassForSourceDeclaration) {
        return kotlinOrigin.isData()
    }
    return false
}

/**
 * Not working for List, String...
 * Works for user defined data classes
 */
fun PsiType.asPsiClass(): PsiClass? = PsiUtil.resolveClassInType(this)  // todo: check against primitive types


/**
 * Checks if the receiver's class type is a Kotlin `List` using the Kotlin Analysis API.
 *
 * This function should be called from a coroutine or another suspending function.
 * It is safe to call from the Event Dispatch Thread (EDT).
 *
 * @see PsiField.isList
 * @return `true` if the receiver is of the Kotlin `List` class type; otherwise, `false`.
 */
suspend fun KtElement.isList(): Boolean {
    return readAction {
        analyze(this) {
            val type = (this as? KtParameter)?.symbol?.returnType
            type?.isClassType(ClassId.fromString("kotlin/collections/List")) == true
        }
    }
}

/**
 * Checks if the receiver's class type is a Kotlin `List` using the Kotlin Analysis API.
 *
 * This function should be called from a coroutine or another suspending function.
 * It is safe to call from the Event Dispatch Thread (EDT).
 *
 * @see KtElement.isList
 * @return `true` if the receiver is of the Kotlin `List` class type; otherwise, `false`.
 */
suspend fun PsiField.isList(): Boolean {
    val ktElement: KtDeclaration = (this as KtLightFieldForSourceDeclarationSupport).kotlinOrigin!!
    return readAction {
        analyze(ktElement) {
            val type = (ktElement as? KtParameter)?.symbol?.returnType
            type?.isClassType(ClassId.fromString("kotlin/collections/List")) == true
        }
    }
}

/**
 *  @param parameterTypeString if the psiFiled is List&lt;User> the parameterTypeString will be "User"
 */
fun PsiField.isKotlinListWithParameterTypeOf(parameterTypeString: String): Boolean {
    return this.type.presentableText == "List<$parameterTypeString>"
}

fun PsiField.isKotlinListWithAnyParameterType(): Boolean {
    return this.type.presentableText.matches(Regex("List<.*>"))
}

fun PsiType.isListType(): Boolean {
    if (this is PsiClassType) {
        val resolvedClass = this.resolve()
        return resolvedClass?.qualifiedName == "java.util.List" || resolvedClass?.qualifiedName == "kotlin.collections.List"
    }
    return false
}



fun extractListParameterType(ktElement: KtElement) {
    ApplicationManager.getApplication().executeOnPooledThread {
        runReadAction {
            analyze(ktElement) {
                val type = ktElement.expectedType
                val isList = ((ktElement as KtParameter).symbol as KaValueParameterSymbol).returnType.isClassType(
                    ClassId.fromString("kotlin/collections/List")
                )
                val argType =
                    ((ktElement.symbol as KaValueParameterSymbol).returnType as KaClassType).typeArguments.first().type

                println(isList)
                println(type)
                println(argType)

                val argClass = ((argType as KaClassType).symbol as KaClassSymbol).sourcePsi<KtClass>()
                println(argClass?.name)
                println(argClass?.isData())
            }
        }
    }
}

suspend fun PsiField.extractListParameterType(): KtClass? {
    val ktElement: KtDeclaration = (this as KtLightFieldForSourceDeclarationSupport).kotlinOrigin!!
        return readAction {
            analyze(ktElement) {
                val type = ktElement.expectedType
                val isList = ((ktElement as KtParameter).symbol as KaValueParameterSymbol).returnType.isClassType(
                    ClassId.fromString("kotlin/collections/List")
                )
                val argType =
                    ((ktElement.symbol as KaValueParameterSymbol).returnType as KaClassType).typeArguments.first().type


                val argClass = ((argType as KaClassType).symbol as KaClassSymbol).sourcePsi<KtClass>()
                println(argClass?.name)
                println(argClass?.isData())
                argClass
            }
    }
}

data class ListInfoHolder(
    val parameterType: KtClass?,
    val containingFile: KtFile?,
)