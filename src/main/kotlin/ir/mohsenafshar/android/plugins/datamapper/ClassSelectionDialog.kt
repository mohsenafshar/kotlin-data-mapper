import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AllClassesSearch
import java.awt.BorderLayout
import javax.swing.*
import com.intellij.ui.dsl.builder.*


class ClassSelectionDialog(private val project: Project) : DialogWrapper(project) {
    private val sourceClassField = ComboBox<String>()
    private val targetClassField = ComboBox<String>()
    private val cNames = mutableListOf<String>()

    init {
        title = "Select Classes for Mapping"
        init()

        // Populate the combo boxes with all class names in the project
        val classNames = getAllClassNames(project)
        cNames.addAll(classNames)
        classNames.forEach {
            sourceClassField.addItem(it)
            targetClassField.addItem(it)
        }
    }

    private fun getAllClassNames(project: Project): List<String> {
        val classes = mutableListOf<String>()
        AllClassesSearch.search(GlobalSearchScope.projectScope(project), project).forEach { psiClass ->
            classes.add(psiClass.qualifiedName ?: "")
        }
        return classes
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            group("Title") {
                row("Row:") {
                    label("DTO")
                    comboBox(getAllClassNames(project))
                }
            }
        }

//        val panel = JPanel(BorderLayout())
//        val sourcePanel = JPanel().apply {
//            add(JLabel("Source Class (DTO):"))
//            add(sourceClassField)
//        }
//        val targetPanel = JPanel().apply {
//            add(JLabel("Target Class (Domain):"))
//            add(targetClassField)
//        }
//        panel.add(sourcePanel, BorderLayout.NORTH)
//        panel.add(targetPanel, BorderLayout.SOUTH)
//        return panel
    }

    fun getSelectedClasses(): Pair<String?, String?> {
        return Pair(sourceClassField.selectedItem as? String, targetClassField.selectedItem as? String)
    }
}
