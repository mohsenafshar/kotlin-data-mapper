package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ir.mohsenafshar.toolkits.jetbrains.ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.MapperProjectService


class MapperAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val dialog = MapperInfoSelectionDialog(project, event)

        if (dialog.showAndGet()) {
            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
            val isExtensionFunction = dialog.isExtensionFunctionSelected()
            val targetFileName = dialog.getSelectedFileName()!! // TODO: HANDLE NULL

            if (sourceClassName != null && targetClassName != null) {
                val mapperConfig = MapperGenerator.Config(isExtensionFunction, targetFileName, sourceClassName, targetClassName)
                project.getService(MapperProjectService::class.java).generate(mapperConfig)
            }
        }
    }
}