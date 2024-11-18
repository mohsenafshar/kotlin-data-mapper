package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import javax.swing.JComponent
import javax.swing.JPanel


class AppSettingsComponent {
    val panel: JPanel
    private val userNameTextField = JBTextField()
    private val ideaUserStatusCheckBox = JBCheckBox("IntelliJ IDEA user")

    init {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("User name:"), userNameTextField, 1, false)
            .addComponent(ideaUserStatusCheckBox, 1)
            .addComponentFillVertically(JPanel(), 0)
            .getPanel()
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
}