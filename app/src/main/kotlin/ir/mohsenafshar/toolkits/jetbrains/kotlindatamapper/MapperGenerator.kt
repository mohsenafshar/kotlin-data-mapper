package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import ir.mohsenafshar.toolkits.jetbrains.ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.models.FieldInfoHolder
import ir.mohsenafshar.toolkits.jetbrains.ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.models.MapperClassInfoHolder
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.data.AppSettings
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.*
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.idea.base.psi.imports.addImport
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

class MapperGenerator(
    private val project: Project,
    private val config: Config,
) {
    private val sb = StringBuilder()
    private var targetFile: KtFile? = null
    private var prefix = "this"
    private var pattern = AppSettings.defaultExtPattern()

    suspend fun generate() {
        val (isExtensionFunction, targetFileName, sourceClassName, targetClassName) = config
        val sourceClass = findPsiClassByFQName(project, sourceClassName)
        val targetClass = findPsiClassByFQName(project, targetClassName)

        if (sourceClass == null || targetClass == null) {
            val notFoundClass = if (sourceClass == null) sourceClassName else targetClassName

            NotificationGroupManager.getInstance()
                .getNotificationGroup("Kotlin Data Mapper")
                .createNotification(
                    "Generating failed",
                    "The \'$notFoundClass\' class was not found",
                    NotificationType.ERROR
                )
                .notify(project)
        } else {
            prefix = if (isExtensionFunction) "this" else sourceClass.name!!.decapitalize()
            targetFile = findKtFileByName(project, targetFileName)
                ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

            val settings = AppSettings.instance.state
            val resultFunction = if (isExtensionFunction) {
                val extPattern: String =
                    settings.userDefinedExtFunctionPattern ?: AppSettings.defaultExtPattern()
                pattern = extPattern
                val mapperClassInfo = MapperClassInfoHolder(
                    sourceClass, sourceClassName.split(".").last(),
                    targetClass, targetClassName.split(".").last(),
                    extPattern
                )
                generateAsExtensionFunction(mapperClassInfo)
            } else {
                val globalPattern: String =
                    settings.userDefinedGlobalFunctionPattern ?: AppSettings.defaultGlobalPattern()
                pattern = globalPattern
                val mapperClassInfo = MapperClassInfoHolder(
                    sourceClass, sourceClassName.split(".").last(),
                    targetClass, targetClassName.split(".").last(),
                    globalPattern
                )
                generateAsGlobalFunction(mapperClassInfo)
            }

            appendGeneratedCode(
                textFunction = resultFunction,
                appendTo = targetFileName,
                sourceClass,
                targetClass
            )
        }
    }

    suspend fun generateAsExtensionFunction(
        mapperClassesInfo: MapperClassInfoHolder
    ): String {
        val bakedPattern = mapperClassesInfo.pattern
            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, mapperClassesInfo.sourceClassName)
            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, mapperClassesInfo.targetClassName)

        sb.append("fun ${mapperClassesInfo.sourceClassName}.$bakedPattern(): ${mapperClassesInfo.targetClassName} { return ${mapperClassesInfo.targetClassName}(")
        walkThroughSourceClassFieldsTree(project, mapperClassesInfo.sourceClass, mapperClassesInfo.targetClass, "")
        sb.append(")}")
        return sb.toString()
    }

    private suspend fun generateAsGlobalFunction(
        mapperClassesInfo: MapperClassInfoHolder
    ): String {
        val bakedPattern = mapperClassesInfo.pattern
            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, mapperClassesInfo.sourceClassName)
            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, mapperClassesInfo.targetClassName)

        sb.append("fun $bakedPattern(${mapperClassesInfo.sourceClassName.decapitalize()}: ${mapperClassesInfo.sourceClassName}): ${mapperClassesInfo.targetClassName} { return ${mapperClassesInfo.targetClassName}(")
        walkThroughSourceClassFieldsTree(project, mapperClassesInfo.sourceClass, mapperClassesInfo.targetClass, "")
        sb.append(")}")
        return sb.toString()
    }

    private suspend fun appendGeneratedCode(
        textFunction: String,
        appendTo: String?,
        sourceClass: PsiClass,
        targetClass: PsiClass
    ) {
        val containingFile =
            targetFile
                ?: findKtFileByName(project, appendTo)
                ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

        writeCommandAction(project, "AppendGeneratedCode") {
            val psiFactory = KtPsiFactory(project)

            containingFile.findDescendantOfType<KtFunction> {
                it.name?.contains("to${targetClass.name}") ?: false && it.receiverTypeReference?.text == sourceClass.name
            }?.apply {
                delete()
            }

            val newFunction = psiFactory.createFunction(textFunction)
            containingFile.add(newFunction)
        }
    }

    private suspend fun walkThroughSourceClassFieldsTree(
        project: Project,
        sourceClass: PsiClass,
        targetClass: PsiClass,
        parentChainName: String,
    ) {
        writeCommandAction(project, "AddImport") {
            targetClass.kotlinFqName?.let { targetFile?.addImport(it, false, null, project) }
        }

        val targetFields = readAction { targetClass.fields }
        targetFields.forEach { targetField ->
            val sourceField = readAction { sourceClass.fields.find { it.name == targetField.name } }

            if (sourceField == null) {
                sb.append("${targetField.name} = null,")
            } else {
                val fieldItemInfoHolder = readAction {
                    RelationFieldInfoHolder(
                        source = FieldInfoHolder.fromPsiFieldInstance(sourceField),
                        target = FieldInfoHolder.fromPsiFieldInstance(targetField),
                    )
                }

                if (fieldItemInfoHolder.isTargetDataClass) {
                    sb.append("${targetField.name} = ${fieldItemInfoHolder.target.typeClassName}(")
                    walkThroughSourceClassFieldsTree(
                        project,
                        fieldItemInfoHolder.source.typeClass!!,
                        fieldItemInfoHolder.target.typeClass!!,
                        "$parentChainName.${targetField.name}",
                    )
                    sb.append(")" + ",")
                } else if (sourceField.isKotlinListWithAnyParameterType()) {
                    val sourceKtClass = sourceField.extractListParameterType()!!

                    val targetKtClass =
                        targetClass.findFieldByName(sourceField.name, false)?.extractListParameterType()!!

                    val data = readAction {
                        object {
                            val sourceClassName = sourceKtClass.name!!
                            val targetClassName = targetKtClass.name!!
                            val sourceClassContainingFileName = sourceKtClass.containingFile.name
                            val sourceFQName = sourceKtClass.fqName!!.asString()
                            val targetFQName = targetKtClass.fqName!!.asString()
                        }
                    }

                    val bakedPattern = pattern
                        .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, data.sourceClassName)
                        .replace(FunctionNamePattern.TARGET_PLACEHOLDER, data.targetClassName)

                    sb.append("${targetField.name} = ${prefix}${parentChainName}.${targetField.name}.map(${data.sourceClassName}::$bakedPattern)" + ",")

                    MapperGenerator(
                        project,
                        Config(
                            true,
                            data.sourceClassContainingFileName,
                            data.sourceFQName,
                            data.targetFQName,
                        )
                    ).generate()
                } else {
                    sb.append("${targetField.name} = ${prefix}${parentChainName}.${targetField.name}" + ",")
                }
            }
        }
    }


    data class Config(
        val isExtensionFunction: Boolean,
        val targetFileName: String,
        val sourceClassName: String,
        val targetClassName: String
    )

    private data class RelationFieldInfoHolder(
        val source: FieldInfoHolder,
        val target: FieldInfoHolder,
    ) {
        val isTargetDataClass: Boolean = source.typeClass.isKotlinDataClass()
    }
}

