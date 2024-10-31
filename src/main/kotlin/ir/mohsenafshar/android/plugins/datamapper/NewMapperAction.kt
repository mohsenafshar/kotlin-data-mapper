package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.KotlinLanguage
import javax.swing.JOptionPane

class NewMapperAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        // Prompt for source and target class names
        val sourceClassName = JOptionPane.showInputDialog("Enter source class name:")
        val targetClassName = JOptionPane.showInputDialog("Enter target class name:")

        if (sourceClassName.isNullOrBlank() || targetClassName.isNullOrBlank()) return

        // Locate classes in the project scope
        val psiFacade = JavaPsiFacade.getInstance(project)
        val sourceClass = psiFacade.findClass(sourceClassName, GlobalSearchScope.allScope(project))
        val targetClass = psiFacade.findClass(targetClassName, GlobalSearchScope.allScope(project))

        if (sourceClass == null || targetClass == null) {
            JOptionPane.showMessageDialog(null, "One or both classes not found.")
            return
        }

        // Option for global or extension function
        val functionType = JOptionPane.showOptionDialog(
            null,
            "Choose function type:",
            "Mapper Function Type",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            arrayOf("Global Function", "Extension Function"),
            "Global Function"
        )
        val isExtensionFunction = functionType == 1

        // Generate and validate mapping code
        val generatedCode = generateMappingCode(sourceClass, targetClass, isExtensionFunction)

        if (generatedCode.isBlank()) {
            JOptionPane.showMessageDialog(null, "No compatible fields for mapping.")
            return
        }

        // Debug output: Show generated code before appending it
        println("Generated Code:\n$generatedCode")
        JOptionPane.showMessageDialog(null, "Generated Code:\n$generatedCode")

        // Append generated code to the containing file
        appendGeneratedMethodToFile(project, targetClass, generatedCode)
    }

    private fun generateMappingCode(sourceClass: PsiClass, targetClass: PsiClass, isExtension: Boolean): String {
        val sourceClassName = sourceClass.name ?: return ""
        val targetClassName = targetClass.name ?: return ""
        val methodName = "as$targetClassName"
        val functionPrefix = if (isExtension) "fun $sourceClassName.$methodName" else "fun $methodName"

        // Generate field mappings only if fields exist in both classes
        val body = targetClass.allFields
            .filter { field -> sourceClass.findFieldByName(field.name, false) != null }
            .joinToString(",\n") { field -> "${field.name} = this.${field.name}" }

        if (body.isBlank()) return ""  // No compatible fields to map

        return """
            $functionPrefix(): $targetClassName {
                return $targetClassName(
                    $body
                )
            }
        """.trimIndent()
    }

    private fun appendGeneratedMethodToFile(project: Project, psiClass: PsiClass, methodText: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            try {
                // Get containing file for the target class
                val containingFile = psiClass.containingFile

                // Use the factory to create a dummy file to parse the method code
                val factory = PsiFileFactory.getInstance(project)
                val dummyFile = factory.createFileFromText(
                    "temp.kt", KotlinLanguage.INSTANCE, methodText
                )

                // Extract the generated method and add it to the containing file
                val newMethod = dummyFile.firstChild
                containingFile.add(newMethod)

            } catch (e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(null, "Error appending method to file: ${e.message}")
            }
        }
    }
}

