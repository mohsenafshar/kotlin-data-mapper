package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import java.awt.*
import javax.swing.*


class NewClassSelectionDialog(private val project: Project, event: AnActionEvent) : DialogWrapper(project) {
    private val sourceClassField = JBTextField().apply {
        text = event.getData(CommonDataKeys.PSI_FILE)?.findDescendantOfType<KtClass>()?.kotlinFqName?.asString() ?: ""
    }
    private val targetClassField = JBTextField()
    private val targetFileField = JBTextField().apply {
        text = event.getData(CommonDataKeys.PSI_FILE)?.name ?: ""
    }
    private val extensionFunctionRadio = JRadioButton("Extension Function", true)
    private val globalFunctionRadio = JRadioButton("Global Function")
    private val fileNameField = JBTextField().apply { isEnabled = false }

    init {
        title = "Generate Mapping Function"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        // Source Class Selection Panel
        val sourcePanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Source Class : ", SwingConstants.LEFT), BorderLayout.WEST)
            add(sourceClassField, BorderLayout.CENTER)
            add(createSelectClassButton(sourceClassField, "Select Source Class"), BorderLayout.EAST)
        }

        // Target Class Selection Panel
        val targetPanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Target Class :  ", SwingConstants.LEFT), BorderLayout.WEST)
            add(targetClassField, BorderLayout.CENTER)
            add(createSelectClassButton(targetClassField, "Select Target Class"), BorderLayout.EAST)
        }

        // Target Class Selection Panel
        val targetFilePanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Generate in :   ", SwingConstants.LEFT), BorderLayout.WEST)
            add(targetFileField, BorderLayout.CENTER)
            add(createSelectFileButton(targetFileField, "Select Mapper Function File"), BorderLayout.EAST)
        }

        // Function Type Selection Panel
        val functionTypePanel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Function Type")
                .apply { titleColor = JBColor(Color.BLACK, Color.WHITE) }
            add(extensionFunctionRadio)
            add(globalFunctionRadio)
        }

        // ButtonGroup for mutual exclusivity
        ButtonGroup().apply {
            add(extensionFunctionRadio)
            add(globalFunctionRadio)
        }

        // Add all components to the main panel
        mainPanel.add(sourcePanel)
        mainPanel.add(targetPanel)
        mainPanel.add(targetFilePanel)
        mainPanel.add(Box.createVerticalStrut(8))
        mainPanel.add(functionTypePanel)

        return mainPanel
    }

    private fun createSelectClassButton(classField: JBTextField, dialogTitle: String): JButton {
        return JButton("...").apply {
            addActionListener {
                val classChooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser(dialogTitle) // todo: Search for kotlin data classes only
                classChooser.showDialog()
                val selectedClass = classChooser.selected
                classField.text = selectedClass?.qualifiedName ?: ""
            }
        }
    }

    private fun createSelectFileButton(fileField: JBTextField, dialogTitle: String): JButton {
        return JButton("...").apply {
            addActionListener {
                val fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
                    dialogTitle, null,
                    KotlinFileType.INSTANCE, null,
                )
                fileChooser.showDialog()
                val selectedFile = fileChooser.selectedFile
                fileField.text = selectedFile?.name ?: ""
            }
        }
    }

    fun getSelectedClasses(): Pair<String?, String?> {
        return Pair(sourceClassField.text, targetClassField.text)
    }

    fun getSelectedFileName(): String? {
        return targetFileField.text
    }

    fun isExtensionFunctionSelected(): Boolean {
        return extensionFunctionRadio.isSelected
    }
}

class RoundedPanel(private val borderColor: Color, private val radius: Int) : JPanel() {
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = borderColor
        g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius) // Use the radius here
    }
}
