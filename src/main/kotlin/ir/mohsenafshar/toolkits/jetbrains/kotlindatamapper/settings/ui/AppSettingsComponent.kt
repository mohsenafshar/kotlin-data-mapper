package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.ui

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.text.findTextRange
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.NotNull
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Style
import javax.swing.text.StyleConstants


class AppSettingsComponent {
    val panel: JPanel
    private val userNameTextField = JBTextField()
    private val ideaUserStatusCheckBox = JBCheckBox("IntelliJ IDEA user")
    private val pTextPane = JTextPane().apply {
        text = "Here is some text with a %SOURCE_CLASS%."
    }.also { onTextChange("init") }

    private var defaultStyle: Style = pTextPane.logicalStyle
    private val customStyle: Style = pTextPane.styledDocument.addStyle("customStyle", null).apply {
        StyleConstants.setForeground(this, JBColor(Color(146, 25, 227), Color(173, 136, 244)))
        StyleConstants.setBold(this, true)
        StyleConstants.setItalic(this, true)
    }

    private var isListenerAdded = false

    init {
        if (pTextPane.styledDocument.getStyle("customStyle") != null) pTextPane.styledDocument.removeStyle("customStyle")

        registerThemeChangeListener {
            defaultStyle = pTextPane.logicalStyle
            onTextChange("LAF")
        }

//        onTextChange("Init")

        if (!isListenerAdded) {
            isListenerAdded = true
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
        }

        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("User name:"), userNameTextField, 1, false)
            .addComponent(ideaUserStatusCheckBox, 1)
            .addComponent(StyledTextPane(pTextPane), 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    private fun onTextChange(where: String) {
        println("Text changed at $where: ${pTextPane.text}")
        SwingUtilities.invokeLater {
            pTextPane.styledDocument.setCharacterAttributes(0, pTextPane.text.length, defaultStyle, true)

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

    // Register the listener in your plugin initialization code
    private fun registerThemeChangeListener(action: () -> Unit) {
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(LafManagerListener.TOPIC, MyThemeChangeListener(action))
    }
}


class StyledTextPane(textPane: JTextPane) : JPanel(BorderLayout()) {
    init {
        textPane.apply {
            background = JBColor.white
//            font = Font("Arial", Font.PLAIN, 12)
            margin = JBUI.insets(3)
        }

        val compoundBorder = CompoundBorder(
            BorderFactory.createLineBorder(JBColor.lightGray, 1, true),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        )

        val textPaneWrapper = JPanel(BorderLayout()).apply {
            border = compoundBorder
            add(textPane)
        }

        val scrollPane = JBScrollPane(textPaneWrapper).apply {
            border = null  // Remove scroll pane border
        }

        add(scrollPane, BorderLayout.CENTER)
    }
}

class MyThemeChangeListener(private val action: () -> Unit) : LafManagerListener {
    override fun lookAndFeelChanged(source: LafManager) {
        // This method is called whenever the theme is changed
        val currentLookAndFeel = UIManager.getLookAndFeel()
        println("Theme changed to: ${currentLookAndFeel.name}")

        action()
    }
}