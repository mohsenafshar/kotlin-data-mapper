package ir.mohsenafshar.toolkits.jetbrains.ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.models

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.asPsiClass

data class FieldInfoHolder(
    val psiField: PsiField,
    val psiFieldName: String,
    val typeClass: PsiClass?,
    val typeClassName: String?,
    val supportedType: SupportedType = SupportedType.NONE,
) {

    companion object {
        fun fromPsiFieldInstance(psiField: PsiField)= FieldInfoHolder(
            psiField = psiField,
            psiFieldName = psiField.name,
            typeClass = psiField.type.asPsiClass(),
            typeClassName = psiField.type.asPsiClass()?.name,
        )
    }
}

data class MapperClassInfoHolder(
    val sourceClass: PsiClass,
    val sourceClassName: String,
    val targetClass: PsiClass,
    val targetClassName: String,
    val pattern: String
)

enum class SupportedType {
    DATA_CLASS, LIST, NONE
}