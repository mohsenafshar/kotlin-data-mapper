package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.NotNull


@State(
    name = "ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.AppSettings",
    storages = [Storage("SdkSettingsPlugin.xml")]
)
class AppSettings : PersistentStateComponent<AppSettings.State> {

    class State {
        var userId: @NonNls String? = "John Smith"
        var ideaStatus: Boolean = false
    }

    private var myState = State()

    override fun getState(): State {
        return myState
    }

    override fun loadState(@NotNull state: State) {
        myState = state
    }

    companion object {
        val instance: AppSettings
            get() = ApplicationManager.getApplication()
                .getService(AppSettings::class.java)
    }
}