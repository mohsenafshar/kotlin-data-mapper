package ir.mohsenafshar.toolkits.jetbrains.ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.MapperGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class MapperProjectService(
    private val project: Project,
    private val cs: CoroutineScope
) {
    fun generate(mapperConfig: MapperGenerator.Config) {
        cs.launch {
            try {
                MapperGenerator(project, mapperConfig).generate()
            } finally {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Kotlin Data Mapper")
                    .createNotification("Mapping function generated", NotificationType.INFORMATION)
                    .notify(project)
            }
        }
    }
}