package ir.mohsenafshar.android.plugins.datamapper


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.idea.KotlinLanguage


class MapperAction : AnAction() {

    private var sb = StringBuilder()

    private fun PsiElement?.isKotlinDataClass(): Boolean {
        this ?: return false

        if (this is KtLightClassForSourceDeclaration) {
            return kotlinOrigin.isData()
        }
        return false
    }

    private fun PsiType.asPsiClass(): PsiClass? = PsiUtil.resolveClassInType(this)

    private fun build(sourceClass: PsiClass, targetClass: PsiClass, parentChainName: String) {

        sourceClass.fields.forEach { sourceField ->
            val targetField = targetClass.fields.find { it.name == sourceField.name }

            if (sourceField.type.asPsiClass().isKotlinDataClass()) {
                if (targetField == null || targetField.type.asPsiClass().isKotlinDataClass().not()) {
                    sb.append("${sourceField.name} = null,")
                } else {
                    sb.append("${sourceField.name} = ${sourceField.type.asPsiClass()?.name}(")
                    build(sourceField.type.asPsiClass()!!,targetField.type.asPsiClass()!!, "$parentChainName.${sourceField.name}")
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

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val dialog = NewClassSelectionDialog(project)

        if (dialog.showAndGet()) {
            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
            val isExtensionFunction = dialog.isExtensionFunctionSelected()

            if (sourceClassName != null && targetClassName != null) {
                val sourceClass = findPsiClass(project, sourceClassName)
                val targetClass = findPsiClass(project, targetClassName)

                if (sourceClass != null && targetClass != null) {
//                    val generatedCode = generateMappingCode(sourceClass, targetClass, isExtensionFunction)
                    sb.append("fun ${sourceClass.name}.to${targetClass.name}(): ${targetClass.name} { return ${targetClass.name}(")
                    build(targetClass, sourceClass, "")
                    sb.append(")}")

                    if (dialog.isSeparateFileGenerationEnabled()) {
                        insertGeneratedCode(project, sb.toString())
                    } else {
                        appendGeneratedCode(project, dialog.getSourceClassDocument()!!, sb.toString())
                    }
                } else {
                    Messages.showMessageDialog(
                        project,
                        "One of the classes ($sourceClassName or $targetClassName) was not found",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
            }
        }
    }

    private fun findPsiClass(project: Project, className: String): PsiClass? {
        return JavaPsiFacade.getInstance(project)
            .findClass(className, GlobalSearchScope.allScope(project))
    }

    private fun generateMappingCode(
        sourceClass: PsiClass,
        targetClass: PsiClass,
        isExtensionFunction: Boolean
    ): String {
        val mappings = targetClass.fields.joinToString(",\n") { field ->
            val sourceField = sourceClass.findFieldByName(field.name, true)
            if (sourceField != null) {
                if (isExtensionFunction) {
                    "${field.name} = this.${field.name}"
                } else {
                    "${field.name} = ${sourceClass.name?.decapitalize()}.${field.name}"
                }
            } else {
                "${field.name} = null"
            }
        }

        return if (isExtensionFunction) {
            """
            fun ${sourceClass.name}.to${targetClass.name}(): ${targetClass.name} {
                return ${targetClass.name}(
                    $mappings
                )
            }
        """.trimIndent()
        } else {
            """
            fun map${sourceClass.name}To${targetClass.name}(${sourceClass.name?.decapitalize()}: ${sourceClass.name}): ${targetClass.name} {
                return ${targetClass.name}(
                    $mappings
                )
            }
        """.trimIndent()
        }
    }

    private fun insertGeneratedCode(project: Project, generatedCode: String) {
        val psiFileFactory = PsiFileFactory.getInstance(project)
        val file = psiFileFactory.createFileFromText(
            "GeneratedMapper.kt",
            KotlinLanguage.INSTANCE,
            generatedCode
        )

        WriteCommandAction.runWriteCommandAction(project) {
            val srcDir = project.baseDir.findFileByRelativePath("src")
            if (srcDir != null) {
                val psiDirectory: PsiDirectory? = PsiManager.getInstance(project).findDirectory(srcDir)
                if (psiDirectory != null) {
                    psiDirectory.add(file)
                } else {
                    Messages.showMessageDialog(
                        project,
                        "PsiDirectory could not be created from the source directory",
                        "Error",
                        Messages.getErrorIcon()
                    )
                }
            } else {
                Messages.showMessageDialog(
                    project,
                    "Source directory not found",
                    "Error",
                    Messages.getErrorIcon()
                )
            }
        }
    }

    private fun appendGeneratedCode(project: Project, document: Document, generatedCode: String) {
        try {
            val newContent: String = document.getText() + generatedCode
            val r = Runnable {
                document.setReadOnly(false)
                document.setText(newContent)
            }
            WriteCommandAction.runWriteCommandAction(project, r)
        } catch (e: Exception) {
        }
    }
}