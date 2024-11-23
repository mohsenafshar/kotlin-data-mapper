package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.ui

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.components.BorderLayoutPanel
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern.Companion.SOURCE_CLASS_PLACEHOLDER
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern.Companion.TARGET_CLASS_PLACEHOLDER
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents.StyledTextPane
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.*
import java.awt.Font
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel


class AppSettingsComponent(defaultExtPattern: String, defaultGlobalPattern: String) : Disposable {
    private val connection = ApplicationManager.getApplication().messageBus.connect(this)

    val panel: JPanel
    private val globalFuncTextPane = StyledTextPane(SOURCE_CLASS_PLACEHOLDER, TARGET_CLASS_PLACEHOLDER)
    private val extFuncTextPane = StyledTextPane(SOURCE_CLASS_PLACEHOLDER, TARGET_CLASS_PLACEHOLDER)

    private val htmlContent =
        "You can define a custom naming pattern using <i>${SOURCE_CLASS_PLACEHOLDER}</i> and <i>${TARGET_CLASS_PLACEHOLDER}</i>.<br>These placeholders will dynamically be replaced with the actual class names<br> when the function is generated.".asHtml()


    init {
        connection.subscribe(LafManagerListener.TOPIC, MyThemeChangeListener {
            globalFuncTextPane.repaintStyle()
            extFuncTextPane.repaintStyle()
        })

        val globalFuncPatternPanel: BorderLayoutPanel =
            functionPatternPanel("Global Function :     ", globalFuncTextPane, defaultGlobalPattern) {
                globalPattern = defaultGlobalPattern
            }

        val extFuncPatternPanel: BorderLayoutPanel =
            functionPatternPanel("Extension Function :", extFuncTextPane, defaultExtPattern) {
                extPattern = defaultExtPattern
            }

        panel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("Function Naming Pattern").marginTop(16).marginBottom(16).apply {
                font = Font("Inter", Font.BOLD, this.font.size + 1)
            })
            .addLabeledComponent(JBLabel("Instruction :"), JBLabel(htmlContent).marginLeft(8).marginTop(4), true)
            .addComponent(globalFuncPatternPanel.marginTop(16))
            .addComponent(extFuncPatternPanel.marginTop(8))
            .addSeparator(16)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    val preferredFocusedComponent: JComponent
        get() = globalFuncTextPane

    var extPattern: String?
        get() = extFuncTextPane.textPane.text
        set(newText) {
            extFuncTextPane.textPane.text = newText
        }

    var globalPattern: String?
        get() = globalFuncTextPane.textPane.text
        set(newText) {
            globalFuncTextPane.textPane.text = newText
        }

    private fun functionPatternPanel(
        label: String,
        styledTextPane: StyledTextPane,
        default: String,
        actionListener: (ActionEvent) -> Unit
    ): BorderLayoutPanel {
        return BorderLayoutPanel(10, 0).apply {
            addToLeft(BorderLayoutPanel().apply {
                addToTop(JBLabel(label).marginTop(10))
            })
            addToCenter(BorderLayoutPanel().apply {
                addToTop(styledTextPane)
                addToBottom(BorderLayoutPanel().apply {
                    marginLeft(5)
                    addToTop(JBLabel(default).apply {
                        marginTop(2)
                        foreground = JBColor.gray
                        smallFont()
                    })
                })
            })
            addToRight(BorderLayoutPanel().apply {
                addToTop(JButton("Reset to Default").apply {
                    addActionListener(actionListener)
                })
            })
        }
    }

    override fun dispose() {
        connection.dispose()
    }

    class MyThemeChangeListener(private val action: () -> Unit) : LafManagerListener {
        override fun lookAndFeelChanged(source: LafManager) {
            action()
        }
    }
}

