package ir.mohsenafshar.android.plugins.datamapper


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.idea.base.psi.imports.addImport
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType


class MapperAction : AnAction() {

    private var sb = StringBuilder()
    private var prefix = "this"
    private var targetFileName = "this"
    private var targetFile: KtFile? = null


    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val dialog = NewClassSelectionDialog(project, event)

        sb = StringBuilder()

        val vfile = dialog.getSelectedFileName()
            ?.let { FilenameIndex.getVirtualFilesByName(it, GlobalSearchScope.allScope(project)) }?.first()

        vfile?.toPsiFile(project)

        if (dialog.showAndGet()) {
            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
            val isExtensionFunction = dialog.isExtensionFunctionSelected()

            if (sourceClassName != null && targetClassName != null) {

                val sourceClass = findPsiClass(project, sourceClassName)
                val targetClass = findPsiClass(project, targetClassName)

                if (sourceClass != null && targetClass != null) {
                    prefix = if (isExtensionFunction) "this" else sourceClass.name!!.decapitalize()
//                    val generatedCode = generateMappingCode(sourceClass, targetClass, isExtensionFunction)

//                    sourceClass.add(targetClass)

                    targetFile = findKtFileByName(project, dialog.getSelectedFileName())
                        ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

                    if (isExtensionFunction) {
                        sb.append("fun ${sourceClass.name}.to${targetClass.name}(): ${targetClass.name} { return ${targetClass.name}(")
                        build(project, targetClass, sourceClass, "")
                        sb.append(")}")
                    } else {
                        sb.append("fun map${sourceClass.name}To${targetClass.name}(${sourceClass.name?.decapitalize()}: ${sourceClass.name}): ${targetClass.name} { return ${targetClass.name}(")
                        build(project, targetClass, sourceClass, "")
                        sb.append(")}")
                    }

                    if (dialog.isSeparateFileGenerationEnabled()) {
                        insertGeneratedCode(project, sb.toString(), sourceClass)
                    } else {
//                        appendGeneratedCode(project, dialog.getSourceClassDocument()!!, sb.toString())

                        appendGeneratedCode1(project, dialog.getSelectedFileName(), sourceClass, targetClass)
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

    private fun appendGeneratedCode1(
        project: Project,
        selectedFileName: String?,
        sourceClass: PsiClass,
        targetClass: PsiClass
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            writeCommandAction(project, "Generate Extension Function") {
                val psiFactory = KtPsiFactory(project)

                val containingFile = targetFile ?: findKtFileByName(project, selectedFileName)
                ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

                containingFile.findDescendantOfType<KtFunction> {
                    it.name?.contains("to${targetClass.name}") ?: false && it.receiverTypeReference?.text == sourceClass.name
                }?.apply {
                    delete()
                }

                val newFunction = psiFactory.createFunction(sb.toString())
                containingFile.add(newFunction)
            }
        }
    }

    private fun build(
        project: Project,
        sourceClass: PsiClass,
        targetClass: PsiClass,
        parentChainName: String,
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            sourceClass.kotlinFqName?.let { targetFile?.addImport(it, false, null, project) }
        }

        sourceClass.fields.forEach { sourceField ->
            val targetField = targetClass.fields.find { it.name == sourceField.name }

            if (sourceField.type.asPsiClass().isKotlinDataClass()) {
                if (targetField == null || targetField.type.asPsiClass().isKotlinDataClass().not()) {
                    sb.append("${sourceField.name} = null,")
                } else {
                    sb.append("${sourceField.name} = ${sourceField.type.asPsiClass()?.name}(")
                    build(
                        project,
                        sourceField.type.asPsiClass()!!,
                        targetField.type.asPsiClass()!!,
                        "$parentChainName.${sourceField.name}",
                    )
                    sb.append(")")
                }
            } else {
                if (targetField == null) {
                    sb.append("${sourceField.name} = null,")
                } else {
                    sb.append("${sourceField.name} = ${prefix}${parentChainName}.${sourceField.name}" + ",")
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

    private fun insertGeneratedCode(project: Project, generatedCode: String, sourceClass: PsiClass) {
        val psiFileFactory = PsiFileFactory.getInstance(project)
        val file = psiFileFactory.createFileFromText(
            "GeneratedMapper.kt",
            KotlinLanguage.INSTANCE,
            generatedCode
        )

        WriteCommandAction.runWriteCommandAction(project) {
            val srcDir = project.baseDir.findFileByRelativePath("src")
            if (srcDir != null) {
                val psiDirectory: PsiDirectory? = sourceClass.containingFile.containingDirectory
                if (psiDirectory != null) {
                    CodeStyleManager.getInstance(project).reformat(file)
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

    private fun findKtFileByName(project: Project, fileName: String?): KtFile? {
        fileName ?: return null

        val ktExtension = KotlinFileType.INSTANCE.defaultExtension
        val ktFile: KtFile? =
            FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.allScope(project)).find { virtualFile ->
                virtualFile.extension == ktExtension && virtualFile.toPsiFile(project) is KtFile
            }?.toPsiFile(project) as KtFile?

        return ktFile
    }

    private fun PsiElement?.isKotlinDataClass(): Boolean {
        this ?: return false

        if (this is KtLightClassForSourceDeclaration) {
            return kotlinOrigin.isData()
        }
        return false
    }

    private fun PsiType.asPsiClass(): PsiClass? = PsiUtil.resolveClassInType(this)
}