package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.ui


import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.*
import javax.swing.text.DefaultStyledDocument
import javax.swing.text.StyleContext


class MyAppSettingsComponent {
    val panel: JPanel
    private val extensionPatternField = JBTextField("to\$TARGET_CLASS$")
    private val globalPatternField = JBTextField("map\$SOURCE_CLASS\$To\$TARGET_CLASS$")
    private val exampleTextArea = JTextArea(3, 30).apply {
        isEditable = false
        text = generateExamples()
    }
    private val previewButton = JButton("Preview")
    private val resetButton = JButton("Reset Defaults")

    init {

        // Add form components
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Extension Function Pattern:"), extensionPatternField, 1, false)
            .addLabeledComponent(JBLabel("Global Function Pattern:"), globalPatternField, 1, false)
            .addLabeledComponent(JBLabel("Examples:"), JScrollPane(exampleTextArea), 1, true)
            .addComponent(previewButton, 1)
            .addComponent(resetButton, 1)
            .addComponentFillVertically(JPanel(), 0)
            .getPanel()

        // Add button actions
        previewButton.addActionListener {
            exampleTextArea.text = generateExamples()
        }

        resetButton.addActionListener {
            resetDefaults()
        }
    }

    val preferredFocusedComponent: JComponent
        get() = extensionPatternField

    var extensionPattern: String
        get() = extensionPatternField.text
        set(newPattern) {
            extensionPatternField.text = newPattern
        }

    var globalPattern: String
        get() = globalPatternField.text
        set(newPattern) {
            globalPatternField.text = newPattern
        }

    private fun resetDefaults() {
        extensionPatternField.text = "to\$TARGET_CLASS$"
        globalPatternField.text = "map\$SOURCE_CLASS\$To\$TARGET_CLASS\$"
        exampleTextArea.text = generateExamples()
    }

    private fun generateExamples(): String {
        val sourceClass = "UserDTO"
        val targetClass = "User"

        val extensionExample = extensionPattern
            .replace("\$SOURCE_CLASS$", sourceClass)
            .replace("\$TARGET_CLASS$", targetClass)
        val globalExample = globalPattern
            .replace("\$SOURCE_CLASS$", sourceClass)
            .replace("\$TARGET_CLASS$", targetClass)

        return """
            Extension Example: $extensionExample
            Global Example: $globalExample
        """.trimIndent()
    }
}
