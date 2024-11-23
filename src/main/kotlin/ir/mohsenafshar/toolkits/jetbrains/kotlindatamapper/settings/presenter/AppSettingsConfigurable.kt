package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.presenter

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.Disposer
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.data.AppSettings
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.ui.AppSettingsComponent
import org.jetbrains.annotations.Nls
import java.util.*
import javax.swing.JComponent


internal class AppSettingsConfigurable : Configurable {
    private var mySettingsComponent: AppSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Kotlin Data Mapper"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = AppSettingsComponent(AppSettings.defaultExtPatternAsHtml(), AppSettings.defaultGlobalPatternAsHtml())
        return mySettingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val state: AppSettings.State =
            Objects.requireNonNull(AppSettings.instance.state)

        return mySettingsComponent!!.globalPattern != state.userDefinedGlobalFunctionPattern ||
                mySettingsComponent!!.extPattern != state.userDefinedExtFunctionPattern
    }

    override fun apply() {
        val state: AppSettings.State =
            Objects.requireNonNull(AppSettings.instance.state)

        state.userDefinedExtFunctionPattern = mySettingsComponent?.extPattern
        state.userDefinedGlobalFunctionPattern = mySettingsComponent?.globalPattern
    }

    override fun reset() {
        val state: AppSettings.State =
            Objects.requireNonNull(AppSettings.instance.state)
        mySettingsComponent?.globalPattern =
            state.userDefinedGlobalFunctionPattern ?: AppSettings.defaultGlobalPattern()
        mySettingsComponent?.extPattern = state.userDefinedExtFunctionPattern ?: AppSettings.defaultExtPattern()
    }

    override fun disposeUIResources() {
        mySettingsComponent?.let { Disposer.dispose(it) }
        mySettingsComponent = null
    }
}