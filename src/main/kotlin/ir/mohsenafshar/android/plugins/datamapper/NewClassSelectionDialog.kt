package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants
import java.awt.BorderLayout

class NewClassSelectionDialog(private val project: Project) : DialogWrapper(project) {
    private var sourceClassField: JBTextField = JBTextField()
    private var targetClassField: JBTextField = JBTextField()

    init {
        title = "Select Classes for Mapping"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())

        // Source Class Selection Button and Field
        val sourcePanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Source Class (DTO):", SwingConstants.LEFT), BorderLayout.WEST)
            add(sourceClassField, BorderLayout.CENTER)
            add(createSelectClassButton(sourceClassField, "Select Source Class (DTO)"), BorderLayout.EAST)
        }

        // Target Class Selection Button and Field
        val targetPanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Target Class (Domain):", SwingConstants.LEFT), BorderLayout.WEST)
            add(targetClassField, BorderLayout.CENTER)
            add(createSelectClassButton(targetClassField, "Select Target Class (Domain)"), BorderLayout.EAST)
        }

        panel.add(sourcePanel, BorderLayout.NORTH)
        panel.add(targetPanel, BorderLayout.SOUTH)
        return panel
    }

    private fun createSelectClassButton(classField: JBTextField, dialogTitle: String): JButton {
        return JButton("...").apply {
            addActionListener {
                val classChooser = TreeClassChooserFactory.getInstance(project).createAllProjectScopeChooser(dialogTitle)
                classChooser.showDialog()
                val selectedClass = classChooser.selected
                classField.text = selectedClass?.qualifiedName ?: ""
            }
        }
    }

    fun getSelectedClasses(): Pair<String?, String?> {
        return Pair(sourceClassField.text, targetClassField.text)
    }
}
