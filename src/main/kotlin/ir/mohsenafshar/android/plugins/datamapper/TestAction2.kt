package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.intellij.psi.util.ClassUtil.findPsiClass
import com.intellij.psi.util.PsiUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.findPropertyByName

class TestAction2 : AnAction() {

    var sb = StringBuilder()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = NewClassSelectionDialog(project)
        val psiManager = PsiManager.getInstance(project)

        if (dialog.showAndGet()) {
            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
            val isExtensionFunction = dialog.isExtensionFunctionSelected()

            if (sourceClassName != null && targetClassName != null) {
                val sourceClass = findPsiClass(psiManager, sourceClassName)
                val targetClass = findPsiClass(psiManager, targetClassName)

                val sourceKtClass = (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin as KtClass
                val targetKtClass = (targetClass as KtLightClassForSourceDeclaration).kotlinOrigin as KtClass

                if (sourceClass != null && targetClass != null) {
                    println("sourceKtClass is data class: ${sourceKtClass.isData()}")
                    println("targetKtClass is data class: ${targetKtClass.isData()}")

                    sb.append("fun ${sourceClass.name}.to${targetClass.name}(): ${targetClass.name} { return ${targetClass.name}(")
                    build2(targetClass, sourceClass, "")
                    sb.append(")}")
//                    generateMapping(sourceKtClass, targetKtClass)
//                        val generatedCode = generateMappingCode(sourceClass, targetClass, isExtensionFunction)
//
//                        if (dialog.isSeparateFileGenerationEnabled()) {
//                            insertGeneratedCode(project, generatedCode)
//                        } else {
//                            appendGeneratedCode(project, dialog.getSourceClassDocument()!!, generatedCode)
//                        }

                    println(sb.toString())
                }
            } else {
                Messages.showMessageDialog(
                    project,
                    "One of the classes ($sourceClassName or $targetClassName) was not found",
                    "Error",
                    Messages.getErrorIcon()
                )
            }
        } else {
            TODO()
        }
    }

//        CoroutineScope(Dispatchers.Default).launch {
//            val psiFile = e.getData(CommonDataKeys.PSI_FILE)!!
//
//            readAction {
//                val classes = psiFile.getChildrenOfType<KtClass>().asList()
//
//                val ktClass = classes.find {
//                    it.name == "User"
//                }!!
//
//                generateMapping(ktClass)
//            }
//
//            println(sb.toString())
//        }

    /**
     * sourceKtClass is usually the DTO Class
     * targetKtClass is usually the Domain/Entity Class
     */
    private fun generateMapping(sourceKtClass: KtClass, targetKtClass: KtClass) {
        CoroutineScope(Dispatchers.Default).launch {
            readAction {
                analyze(targetKtClass) {
                    val constructorSymbol = targetKtClass.primaryConstructor!!.symbol
                    sb.append("fun ${sourceKtClass.name}.to${targetKtClass.name}(): ${targetKtClass.name} { return ${targetKtClass.name}(")

                    constructorSymbol.valueParameters.forEach {
                        val sourceParameter: KaValueParameterSymbol? =
                            sourceKtClass.findPropertyByName(it.name.asString())?.symbol as KaValueParameterSymbol?
                        println("sourceParameter=$sourceParameter")
                        build(it, sourceParameter, "")
                    }
                    sb.append(")}")
                }
            }

            println(sb.toString())
        }
    }

    private fun generateMapping(sourceClass: PsiClass, targetClass: PsiClass) {
        CoroutineScope(Dispatchers.Default).launch {
            readAction {
                sb.append("fun ${sourceClass.name}.to${targetClass.name}(): ${targetClass.name} { return ${targetClass.name}(")

//                sourceClass.fields.forEach {
//                    build2(it, sourceParameter,"")
//                }

                sb.append(")}")
            }

            println(sb.toString())
        }
    }

    private fun build2(sourceClass: PsiClass, targetClass: PsiClass, parentChainName: String) {

        sourceClass.fields.forEach { sourceField ->
            val targetField = targetClass.fields.find { it.name == sourceField.name }

            if (sourceField.type.asPsiClass().isKotlinDataClass()) {
                if (targetField == null || targetField.type.asPsiClass().isKotlinDataClass().not()) {
                    sb.append("${sourceField.name} = null,")
                } else {
                    sb.append("${sourceField.name} = ${sourceField.type.asPsiClass()?.name}(")
                    build2(sourceField.type.asPsiClass()!!,targetField.type.asPsiClass()!!, "$parentChainName.${sourceField.name}")
                    sb.append(")")
                }
            } else {
                if (targetField == null) {
                    sb.append("${sourceField.name} = null,")
                } else {
                    sb.append("${sourceField.name} = this${parentChainName}.${sourceField.name}" + ",")
                }
            }

        }
    }

    private fun PsiElement?.isKotlinDataClass(): Boolean {
        this ?: return false

        if (this is KtLightClassForSourceDeclaration) {
            return kotlinOrigin.isData()
        }
        return false
    }

    private fun PsiType.asPsiClass(): PsiClass? = PsiUtil.resolveClassInType(this)

    private fun build(
        parameter: KaValueParameterSymbol,
        sourceParameter: KaValueParameterSymbol?,
        parentChainName: String
    ) {
        val parameterName = parameter.name

        if (sourceParameter == null) {
            sb.append("$parameterName = null,")
            return
        }

        sb.append("$parameterName = ")

        if (parameter.isDataClass()) {
            val ktClass = parameter.returnType.symbol?.psi as KtClass
            val ktSourceClass = sourceParameter.returnType.symbol?.psi as KtClass

            sb.append("${ktClass.name}(")

            analyze(ktClass) {
                val parameters: List<KaValueParameterSymbol> =
                    ktClass.primaryConstructor?.symbol?.valueParameters ?: emptyList()
                val nextSourceParameter =
                    ktSourceClass.findPropertyByName(parameterName.asString())?.symbol as KaValueParameterSymbol?
                parameters.forEach {
                    build(it, nextSourceParameter, "$parentChainName.$parameterName")
                }
            }

            sb.append(")")
        } else {
            sb.append("this${parentChainName}.$parameterName" + ",") // todo: handle parameter.name as null string
        }
    }
}


private fun KaValueParameterSymbol.isDataClass(): Boolean {
    val parameterSymbol = returnType.symbol
    return if (parameterSymbol is KaNamedClassSymbol) {
        parameterSymbol.isData
    } else {
        false
    }
}