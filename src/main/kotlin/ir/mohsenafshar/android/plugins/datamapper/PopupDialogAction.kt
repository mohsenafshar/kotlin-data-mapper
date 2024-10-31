package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.showYesNoDialog


class PopupDialogAction: AnAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
    }

    override fun actionPerformed(event: AnActionEvent) {
//        showYesNoDialog("Title", "Message content", project= event.project,
//            yesText = "Yes", noText = "No")

        val message: StringBuilder =
            StringBuilder(event.getPresentation().getText() + " Selected!")

        val selectedElement = event.getData(CommonDataKeys.NAVIGATABLE)
        if (selectedElement != null) {
            message.append("\nSelected Element: ").append(selectedElement)
        }

        val title = event.presentation.description
        Messages.showMessageDialog(
            event.project,
            message.toString(),
            title,
            Messages.getInformationIcon()
        )
    }
}