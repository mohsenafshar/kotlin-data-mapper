package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.text.findTextRange
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.margin
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.addBorder
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Style
import javax.swing.text.StyleConstants


class StyledTextPane(private val sourcePlaceHolder: String, private val targetPlaceHolder: String) :
    JPanel(BorderLayout()) {

    val textPane: JTextPane = JTextPane()

    val roundedBorder = RoundedBorder(8)
    val defaultStroke = BasicStroke(1.0f)
    val focusStroke = BasicStroke(2.0f)

    private var defaultStyle: Style = textPane.logicalStyle
    private val customStyle: Style = textPane.styledDocument.addStyle("customStyle", null).apply {
        StyleConstants.setForeground(this, JBColor(Color(146, 25, 227), Color(173, 136, 244)))
        StyleConstants.setItalic(this, true)
    }

    init {
        textPane.apply {
            background = JBColor.white
            margin = JBUI.insets(6, 8)

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

        repaintStyle("Init")
    }

    fun repaintStyle(where: String = "") {
        println("Text changed at $where: ${textPane.text}")
        SwingUtilities.invokeLater {
            textPane.styledDocument.setCharacterAttributes(0, textPane.text.length, defaultStyle, true)

            // todo:  %SOURCE_CLASS%TARGET_CLASS%
            textPane.text.findTextRange(sourcePlaceHolder)?.run {
                textPane.styledDocument.setCharacterAttributes(this.startOffset, this.length, customStyle, false)
            }
            textPane.text.findTextRange(targetPlaceHolder)?.run {
                textPane.styledDocument.setCharacterAttributes(this.startOffset, this.length, customStyle, false)
            }
        }
    }
}