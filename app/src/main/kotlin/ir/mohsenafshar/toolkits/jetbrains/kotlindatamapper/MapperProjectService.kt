package ir.mohsenafshar.toolkits.jetbrains.ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.MapperConfig
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.MapperGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class MapperProjectService(
    private val project: Project,
    private val cs: CoroutineScope
) {
    fun generate(mapperConfig: MapperConfig) {
        cs.launch {
            MapperGenerator(project, mapperConfig).generate()
        }
    }
}