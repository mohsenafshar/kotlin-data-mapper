package ir.mohsenafshar.android.plugins.datamapper

//import ClassSelectionDialog

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement


fun addFunctionToVirtualFile(project: Project, sourceClass: PsiElement) {
    val ktElement = sourceClass as? KtElement
    val ktFile = ktElement?.containingKtFile
    val ktClass = sourceClass as? KtClass

    if (ktFile != null && ktClass != null) {
        val virtualFile: VirtualFile? = ktFile.virtualFile

        if (virtualFile != null) {
            WriteCommandAction.runWriteCommandAction(project) {
                val document = FileDocumentManager.getInstance().getDocument(virtualFile)

                if (document != null) {
                    // Append the new function at the end of the document content
                    val newFunction = """
                        fun printHello() {
                            println("Hello")
                        }
                    """.trimIndent()

                    ApplicationManager.getApplication().runWriteAction {
                        document.insertString(document.textLength, "\n\n$newFunction")
                        FileDocumentManager.getInstance().saveDocument(document)
                    }
                } else {
                    println("Failed to retrieve document from VirtualFile.")
                }
            }
        } else {
            println("Failed to retrieve VirtualFile from KtFile.")
        }
    } else {
        println("Failed to get KtFile or KtClass from the source element.")
    }
}

private fun my(event: AnActionEvent) {
    val project: Project = event.getProject()!!
    val file: VirtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE)!!
    val document: Document = event.getData(PlatformDataKeys.EDITOR)?.getDocument()!!
    try {
        val newContent: String = document.getText() + "fun print(){ println(\"Hello world\") }"
        val r = Runnable {
            document.setReadOnly(false)
            document.setText(newContent)
        }
        WriteCommandAction.runWriteCommandAction(project, r)
    } catch (e: Exception) {
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


class MapperAction : AnAction() {

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
                    val generatedCode = generateMappingCode(sourceClass, targetClass, isExtensionFunction)

                    if (dialog.isSeparateFileGenerationEnabled()) {
                        insertGeneratedCode(project, generatedCode)
                    } else {
                        appendGeneratedCode(project,dialog.getSourceClassDocument()!!, generatedCode)
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
}