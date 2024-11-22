package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.text.findTextRange
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Style
import javax.swing.text.StyleConstants


class StyledTextPane : JPanel(BorderLayout()) {
    var textPane: JTextPane = JTextPane().apply { text = "map\$SOURCE_CLASS\$To\$TARGET_CLASS\$" }
        private set

    val roundedBorder = RoundedBorder(8)
    val defaultStroke = BasicStroke(1.0f)
    val focusStroke = BasicStroke(2.0f)

    private var defaultStyle: Style = textPane.logicalStyle
    private val customStyle: Style = textPane.styledDocument.addStyle("customStyle", null).apply {
        StyleConstants.setForeground(this, JBColor(Color(146, 25, 227), Color(173, 136, 244)))
        StyleConstants.setBold(this, true)
        StyleConstants.setItalic(this, true)
    }

    init {
        textPane.apply {
            background = JBColor.white
            margin = JBUI.insets(3)

            styledDocument.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    repaintStyle("insertUpdate")
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    repaintStyle("removeUpdate")
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    println("changedUpdate")
                }
            })
        }

        val textPaneWrapper = JPanel(BorderLayout()).apply {
            border = roundedBorder
            add(textPane)

            isFocusable = true
            requestFocusInWindow()

            textPane.addFocusListener(object : java.awt.event.FocusListener {
                override fun focusGained(e: java.awt.event.FocusEvent) {
                    roundedBorder.setColor(Color(50, 115, 255))
                    roundedBorder.setStroke(focusStroke)
                    repaint()
                }

                override fun focusLost(e: java.awt.event.FocusEvent) {
                    roundedBorder.setColor(JBColor.lightGray)
                    roundedBorder.setStroke(defaultStroke)
                    repaint()
                }
            })
        }

        val scrollPane = JBScrollPane(textPaneWrapper).apply {
            border = null  // Remove scroll pane border
        }

        add(scrollPane, BorderLayout.CENTER)

        repaintStyle("Init")
    }

    fun repaintStyle(where: String = "") {
        println("Text changed at $where: ${textPane.text}")
        SwingUtilities.invokeLater {
            textPane.styledDocument.setCharacterAttributes(0, textPane.text.length, defaultStyle, true)

            // todo:  %SOURCE_CLASS%TARGET_CLASS%
            textPane.text.findTextRange("\$SOURCE_CLASS$")?.run {
                textPane.styledDocument.setCharacterAttributes(this.startOffset, this.length, customStyle, false)
            }
            textPane.text.findTextRange("\$TARGET_CLASS$")?.run {
                textPane.styledDocument.setCharacterAttributes(this.startOffset, this.length, customStyle, false)
            }
        }
    }
}