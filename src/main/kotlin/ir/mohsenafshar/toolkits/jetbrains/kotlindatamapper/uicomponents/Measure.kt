package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uicomponents

import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.addBorder
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel


fun <C : JComponent> C.margin(insets: JBInsets): C = apply {
    addBorder(JBEmptyBorder(insets))
}

fun <C : JComponent> C.marginLeft(m: Int): C = apply {
    addBorder(JBEmptyBorder(JBUI.insetsLeft(m)))
}

fun <C : JComponent> C.marginTop(m: Int): C = apply {
    addBorder(JBEmptyBorder(JBUI.insetsTop(m)))
}

fun <C : JComponent> C.marginBottom(m: Int): C = apply {
    addBorder(JBEmptyBorder(JBUI.insetsBottom(m)))
}

fun JLabel.smallFont(): JLabel = apply {
    font = Font("Inter", Font.PLAIN, 12)
}