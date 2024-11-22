package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.ui

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.components.BorderLayoutPanel
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.ExtensionFunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.GlobalFunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents.StyledTextPane
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.*
import org.jetbrains.annotations.NotNull
import java.awt.Font
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel


class AppSettingsComponent : Disposable {
    private val connection = ApplicationManager.getApplication().messageBus.connect()

    private val extensionFunctionNamePattern = ExtensionFunctionNamePattern()
    private val globalFunctionNamePattern = GlobalFunctionNamePattern()

    val panel: JPanel
    private val userNameTextField = JBTextField()
    private val ideaUserStatusCheckBox = JBCheckBox("IntelliJ IDEA user")

    private val globalFuncTextPane = StyledTextPane().apply {
        textPane.text = globalFunctionNamePattern.defaultPattern
    }
    private val extFuncTextPane = StyledTextPane().apply {
        textPane.text = extensionFunctionNamePattern.defaultPattern
    }

    private val htmlContent =
        "You can define a custom naming pattern using <i>\$SOURCE_CLASS$</i> and <i>\$TARGET_CLASS$</i>.<br>These placeholders will dynamically be replaced with the actual class names<br> when the function is generated.".asHtml()


    init {
        connection.subscribe(LafManagerListener.TOPIC, MyThemeChangeListener {
            globalFuncTextPane.repaintStyle()
            extFuncTextPane.repaintStyle()
        })

        val globalFuncPatternPanel: BorderLayoutPanel =
            functionPatternPanel("Global Function :     ", globalFuncTextPane, globalFunctionNamePattern)
        val extFuncPatternPanel: BorderLayoutPanel =
            functionPatternPanel("Extension Function :", extFuncTextPane, extensionFunctionNamePattern, true)

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("User name:"), userNameTextField, 1, false)
            .addComponent(ideaUserStatusCheckBox)
            .addSeparator(16)
            .addComponent(JBLabel("Function Naming Pattern").marginTop(16).marginBottom(16).apply {
                font = Font("Inter", Font.BOLD, this.font.size + 1)
            })
            .addLabeledComponent(JBLabel("Instruction :"), JBLabel(htmlContent).marginLeft(8).marginTop(4), true)
            .addComponent(globalFuncPatternPanel.marginTop(12))
            .addComponent(extFuncPatternPanel.marginTop(8))
            .addSeparator(16)
            .addComponentFillVertically(JPanel(), 0)
            .panel
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
        styledTextPane: StyledTextPane,
        functionNamePattern: FunctionNamePattern,
        showExample: Boolean = false
    ): BorderLayoutPanel {
        return BorderLayoutPanel(10, 0).apply {
            addToLeft(BorderLayoutPanel().apply {
                addToTop(JBLabel(label).marginTop(10))
            })
            addToCenter(BorderLayoutPanel().apply {
                addToTop(styledTextPane)
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

