package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.border.CompoundBorder


class StyledTextPane(textPane: JTextPane) : JPanel(BorderLayout()) {
    val roundedBorder = RoundedBorder(8)
    val defaultStroke = BasicStroke(1.0f)
    val focusStroke = BasicStroke(2.0f)

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
            border = roundedBorder
            add(textPane)

            isFocusable = true
            requestFocusInWindow()

            // Add focus listeners to change the border color
            textPane.addFocusListener(object : java.awt.event.FocusListener {
                override fun focusGained(e: java.awt.event.FocusEvent) {
//                    roundedBorder.setColor(JBColor.blue)
                    roundedBorder.setColor(Color(50, 115, 255))
                    roundedBorder.setStroke(focusStroke)
                    repaint() // Repaint to reflect the color change
                }

                override fun focusLost(e: java.awt.event.FocusEvent) {
                    roundedBorder.setColor(JBColor.lightGray)
                    roundedBorder.setStroke(defaultStroke)
                    repaint() // Repaint to revert the color change
                }
            })
        }

        val scrollPane = JBScrollPane(textPaneWrapper).apply {
            border = null  // Remove scroll pane border
        }

        add(scrollPane, BorderLayout.CENTER)
    }
}