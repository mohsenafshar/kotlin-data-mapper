package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.border.Border


class RoundedBorder(
    private val radius: Int,
    private var stroke: BasicStroke = BasicStroke(1.0f),
    private var color: Color = JBColor.lightGray
) : Border {

//    override fun getBorderInsets(c: Component): Insets =
//        Insets(radius + stroke.lineWidth.toInt(), radius + stroke.lineWidth.toInt(), radius + stroke.lineWidth.toInt(), radius + stroke.lineWidth.toInt())

    override fun getBorderInsets(c: Component): Insets = JBUI.insets(4, 6)

    override fun isBorderOpaque(): Boolean = false

    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2.color = color
        g2.stroke = stroke
        g2.drawRoundRect(
            x + stroke.lineWidth.toInt() / 2,
            y + stroke.lineWidth.toInt() / 2,
            width - stroke.lineWidth.toInt(),
            height - stroke.lineWidth.toInt(),
            radius,
            radius
        )
    }

    fun setStroke(newStroke: BasicStroke) {
        this.stroke = newStroke
    }

    fun setColor(newColor: Color) {
        this.color = newColor
    }
}