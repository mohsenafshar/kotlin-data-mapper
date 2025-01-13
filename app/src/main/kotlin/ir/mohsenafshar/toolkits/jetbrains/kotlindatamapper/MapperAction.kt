package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class MapperAction : AnAction() {

    private lateinit var mapperGenerator: MapperGenerator

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val dialog = MapperInfoSelectionDialog(project, event)

        mapperGenerator = MapperGenerator(project)

        if (dialog.showAndGet()) {
            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
            val isExtensionFunction = dialog.isExtensionFunctionSelected()
            val targetFileName = dialog.getSelectedFileName()!! // TODO: HANDLE NULL

            if (sourceClassName != null && targetClassName != null) {
                mapperGenerator.generate(
                    isExtensionFunction,
                    targetFileName,
                    sourceClassName,
                    targetClassName,
                )
            }
        }
    }
}