package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import java.util.*
import javax.swing.JComponent


internal class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "SDK: Application Settings Example"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent()
        return mySettingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val state: AppSettings.State =
            Objects.requireNonNull(AppSettings.instance.state)
        return mySettingsComponent!!.userNameText != state.userId ||
                mySettingsComponent!!.ideaUserStatus != state.ideaStatus
    }

    override fun apply() {
        val state: AppSettings.State =
            Objects.requireNonNull(AppSettings.instance.state)
        state.userId = mySettingsComponent?.userNameText
        state.ideaStatus = mySettingsComponent?.ideaUserStatus ?: false
    }

    override fun reset() {
        val state: AppSettings.State =
            Objects.requireNonNull(AppSettings.instance.state)
        mySettingsComponent?.userNameText = state.userId
        mySettingsComponent?.ideaUserStatus = state.ideaStatus
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}