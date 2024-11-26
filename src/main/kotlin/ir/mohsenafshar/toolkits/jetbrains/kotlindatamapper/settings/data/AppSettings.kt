package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.data

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.ExtensionFunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.GlobalFunctionNamePattern
import org.jetbrains.annotations.NotNull


@State(
    name = "ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.data.AppSettings",
    storages = [Storage("SdkSettingsPlugin.xml")]
)
class AppSettings : PersistentStateComponent<AppSettings.State> {

    private val extensionFunctionNamePattern = ExtensionFunctionNamePattern()
    private val globalFunctionNamePattern = GlobalFunctionNamePattern()

    class State {
        var userDefinedExtFunctionPattern: String? = null
        var userDefinedGlobalFunctionPattern: String? = null
    }

    private var myState = State()

    override fun getState(): State {
        UISettings
        return myState
    }

    override fun loadState(@NotNull state: State) {
        myState = state
    }

    companion object {
        val instance: AppSettings
            get() = ApplicationManager.getApplication().getService(AppSettings::class.java)

        fun defaultExtPattern(): String = instance.extensionFunctionNamePattern.defaultPattern()
        fun defaultExtPatternAsHtml(): String = instance.extensionFunctionNamePattern.defaultPatternAsHtml()
        fun defaultGlobalPattern(): String = instance.globalFunctionNamePattern.defaultPattern()
        fun defaultGlobalPatternAsHtml(): String = instance.globalFunctionNamePattern.defaultPatternAsHtml()
    }
}