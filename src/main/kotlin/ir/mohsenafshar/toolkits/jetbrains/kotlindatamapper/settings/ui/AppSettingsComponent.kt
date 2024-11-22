package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.ui

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.text.findTextRange
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.components.BorderLayoutPanel
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.ExtensionFunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.GlobalFunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents.*
import org.jetbrains.annotations.NotNull
import java.awt.Color
import java.awt.Font
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Style
import javax.swing.text.StyleConstants


class AppSettingsComponent : Disposable {
    private val connection = ApplicationManager.getApplication().messageBus.connect()

    val panel: JPanel
    private val userNameTextField = JBTextField()
    private val ideaUserStatusCheckBox = JBCheckBox("IntelliJ IDEA user")
    private val pTextPane = JTextPane().apply {
        text = "Here is some text with a %SOURCE_CLASS%."
    }

    private var defaultStyle: Style = pTextPane.logicalStyle
    private val customStyle: Style = pTextPane.styledDocument.addStyle("customStyle", null).apply {
        StyleConstants.setForeground(this, JBColor(Color(146, 25, 227), Color(173, 136, 244)))
        StyleConstants.setBold(this, true)
        StyleConstants.setItalic(this, true)
    }

    private val extensionFunctionNamePattern = ExtensionFunctionNamePattern()
    private val globalFunctionNamePattern = GlobalFunctionNamePattern()


    private val htmlContent = "<html>" +
            "<head>" +
            "<style>" +
            "p { padding-top: '2px'; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<h2 style=\"margin-bottom: '0.25rem'\">Instructions</h2>" +
            "<p>You can define a custom naming pattern using <i>\$SOURCE_CLASS$</i> and <i>\$TARGET_CLASS$</i>.<br>These placeholders will dynamically be replaced with the actual class names<br> when the function is generated.</p>" +
            "<ul>" +
            "  <li><b>Global Function</b>" +
            "    <div style=\"margin-left: '16px'; padding-top: '4px'\">" +
            "      <p>Pattern:</p>" +
            "      <code>&nbsp;&nbsp;&nbsp;&nbsp;map\$SOURCE_CLASS\$To\$TARGET_CLASS$</code>" +
            "      <p>Generates:</p>" +
            "      <code>&nbsp;&nbsp;&nbsp;&nbsp;fun mapSourceToTarget(source: Source): Target</code>" +
            "      <p>Example:</p>" +
            "      <code>&nbsp;&nbsp;&nbsp;&nbsp;fun mapUserDtoToUserEntity(userDto: UserDto): UserEntity</code>" +
            "    </div>" +
            "  </li><br>" +
            "  <li><b>Extension Function</b>" +
            "    <div style=\"margin-left: '16px'; padding-top: '4px'\">" +
            "      <p>Pattern:</p>" +
            "      <code>&nbsp;&nbsp;&nbsp;&nbsp;to\$TARGET_CLASS$</code>" +
            "      <p>Generates:</p>" +
            "      <code>&nbsp;&nbsp;&nbsp;&nbsp;fun Source.toTarget(): Target</code>" +
            "      <p>Example:</p>" +
            "      <code>&nbsp;&nbsp;&nbsp;&nbsp;fun UserDto.toUserEntity(): UserEntity</code>" +
            "    </div>" +
            "  </li>" +
            "</ul>" +
            "</body>" +
            "</html>"


    init {
        connection.subscribe(LafManagerListener.TOPIC, MyThemeChangeListener {
            onTextChange("LAF")
        })

        onTextChange("Init")

        pTextPane.styledDocument.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                onTextChange("insertUpdate")
            }

            override fun removeUpdate(e: DocumentEvent?) {
                onTextChange("removeUpdate")
            }

            override fun changedUpdate(e: DocumentEvent?) {
                println("changedUpdate")
            }
        })

        val globalFuncPatternPanel: BorderLayoutPanel =
            functionPatternPanel("Global Function :     ", globalFunctionNamePattern).apply {
                marginTop(8)
                marginBottom(12)
            }
        val extFuncPatternPanel: BorderLayoutPanel =
            functionPatternPanel("Extension Function :", extensionFunctionNamePattern, true)

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("User name:"), userNameTextField, 1, false)
            .addComponent(ideaUserStatusCheckBox)
            .addComponent(StyledTextPane(pTextPane))
            .addSeparator(16)
            .addComponent(JBLabel("Function Naming Pattern").marginTop(16).marginBottom(16).apply {
                font = Font("Inter", Font.BOLD, this.font.size + 1)
            })
            .addComponent(globalFuncPatternPanel)
            .addComponent(extFuncPatternPanel)
            .addComponent(JBLabel(htmlContent))
            .addSeparator(16)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun onTextChange(where: String) {
        println("Text changed at $where: ${pTextPane.text}")
        SwingUtilities.invokeLater {
            pTextPane.styledDocument.setCharacterAttributes(0, pTextPane.text.length, defaultStyle, true)

            // todo:  %SOURCE_CLASS%TARGET_CLASS%
            pTextPane.text.findTextRange("%SOURCE_CLASS%")?.run {
                pTextPane.styledDocument.setCharacterAttributes(this.startOffset, this.length, customStyle, false)
            }
            pTextPane.text.findTextRange("%TARGET_CLASS%")?.run {
                pTextPane.styledDocument.setCharacterAttributes(this.startOffset, this.length, customStyle, false)
            }
        }
    }

    val preferredFocusedComponent: JComponent
        get() = userNameTextField

    @get:NotNull
    var userNameText: String?
        get() = userNameTextField.text
        set(newText) {
            userNameTextField.text = newText
        }

    var ideaUserStatus: Boolean
        get() = ideaUserStatusCheckBox.isSelected
        set(newStatus) {
            ideaUserStatusCheckBox.isSelected = newStatus
        }

    private fun functionPatternPanel(
        label: String,
        functionNamePattern: FunctionNamePattern,
        showExample: Boolean = false
    ): BorderLayoutPanel {
        return BorderLayoutPanel(10, 0).apply {
            addToLeft(BorderLayoutPanel().apply {
                addToTop(JBLabel(label).marginTop(10))
            })
            addToCenter(BorderLayoutPanel().apply {
                addToTop(JBTextField())
                addToBottom(BorderLayoutPanel().apply {
                    marginLeft(5)
                    addToTop(JBLabel(functionNamePattern.defaultPatternAsExpression).apply {
                        marginTop(2)
                        foreground = JBColor.gray
                        smallFont()
                    })
                    if (showExample) {
                        addToBottom(JBLabel(functionNamePattern.examplePatternExplanation).apply {
                            marginTop(4)
                            foreground = JBColor.gray
                            smallFont()
                        })
                    }
                })
            })
            addToRight(BorderLayoutPanel().apply {
                addToTop(JButton("Reset to Default"))
            })
        }
    }

    class MyThemeChangeListener(private val action: () -> Unit) : LafManagerListener {
        override fun lookAndFeelChanged(source: LafManager) {
            action()
        }
    }

    override fun dispose() {
        connection.dispose()
    }
}

