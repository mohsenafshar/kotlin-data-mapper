package ir.mohsenafshar.android.plugins.datamapper

//import ClassSelectionDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.KotlinLanguage

class MapperAction : AnAction() {
//    override fun actionPerformed(event: AnActionEvent) {
//        val project = event.project ?: return
//
//        // Show the class selection dialog
//        val dialog = ClassSelectionDialog(project)
//        if (dialog.showAndGet()) {
//            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
//
//            if (sourceClassName != null && targetClassName != null) {
//                val sourceClass = findPsiClass(project, sourceClassName)
//                val targetClass = findPsiClass(project, targetClassName)
//
//                if (sourceClass != null && targetClass != null) {
//                    val generatedCode = generateMappingCode(sourceClass, targetClass)
//                    insertGeneratedCode(project, generatedCode)
//                } else {
//                    Messages.showMessageDialog(
//                        project,
//                        "One or both classes not found",
//                        "Error",
//                        Messages.getErrorIcon()
//                    )
//                }
//            }
//        }
//    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val dialog = NewClassSelectionDialog(project)

        if (dialog.showAndGet()) {
            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
            if (sourceClassName != null && targetClassName != null) {
                val sourceClass = findPsiClass(project, sourceClassName)
                val targetClass = findPsiClass(project, targetClassName)

                if (sourceClass != null && targetClass != null) {
                    val generatedCode = generateMappingCode(sourceClass, targetClass)
                    insertGeneratedCode(project, generatedCode)
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

//    private fun findPsiClass(project: Project, className: String): PsiClass? {
//        return JavaPsiFacade.getInstance(project)
//            .findClass(className, GlobalSearchScope.allScope(project))
//    }

//    private fun generateMappingCode(sourceClass: PsiClass, targetClass: PsiClass): String {
//        val sourceFields = sourceClass.fields.associateBy { it.name }
//        val targetFields = targetClass.fields.map { it.name to it }.toMap()
//
//        val mappings = targetFields.keys.intersect(sourceFields.keys).joinToString(",\n") { fieldName ->
//            "$fieldName = ${sourceClass.name?.decapitalize()}.${fieldName}"
//        }
//
//        return """
//            fun map${sourceClass.name}To${targetClass.name}(${sourceClass.name?.decapitalize()}: ${sourceClass.name}): ${targetClass.name} {
//                return ${targetClass.name}(
//                    $mappings
//                )
//            }
//        """.trimIndent()
//    }

    private fun generateMappingCode(sourceClass: PsiClass, targetClass: PsiClass): String {
        val mappings = targetClass.fields.joinToString(",\n") { field ->
            val sourceField = sourceClass.findFieldByName(field.name, true)
            if (sourceField != null) {
                "${field.name} = ${sourceClass.name?.decapitalize()}.${field.name}"
            } else {
//                "// Field ${field.name} not found in source class"
                "${field.name} = null"
            }
        }

        return """
            fun map${sourceClass.name}To${targetClass.name}(${sourceClass.name?.decapitalize()}: ${sourceClass.name}): ${targetClass.name} {
                return ${targetClass.name}(
                    $mappings
                )
            }
        """//.trimIndent()
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