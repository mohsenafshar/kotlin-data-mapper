package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.data.AppSettings
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.asPsiClass
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.decapitalize
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.isKotlinDataClass
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.psi.imports.addImport
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

data class MapperConfig(
    val isExtensionFunction: Boolean,
    val targetFileName: String,
    val sourceClassName: String,
    val targetClassName: String
)

class MapperGenerator(
    private val project: Project,
    private val config: MapperConfig,
) {
    private var targetFile: KtFile? = null
    private var prefix = "this"
    private var pattern = "this"

    suspend fun generate() {
        val (isExtensionFunction, targetFileName, sourceClassName, targetClassName) = config
        val sourceClass = findPsiClass(project, sourceClassName)
        val targetClass = findPsiClass(project, targetClassName)

        if (sourceClass != null && targetClass != null) {
            prefix = if (isExtensionFunction) "this" else sourceClass.name!!.decapitalize()
            targetFile = findKtFileByName(project, targetFileName)
                ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

            val settings = AppSettings.instance.state
            val resultFunction = if (isExtensionFunction) {
                val extPattern: String =
                    settings.userDefinedExtFunctionPattern ?: AppSettings.defaultExtPattern()
                pattern = extPattern
                generateAsExtensionFunction(project, sourceClass, targetClass, extPattern)
            } else {
                val globalPattern: String =
                    settings.userDefinedGlobalFunctionPattern ?: AppSettings.defaultGlobalPattern()
                pattern = globalPattern
                generateAsGlobalFunction(project, sourceClass, targetClass, globalPattern)
            }

            try {
                appendGeneratedCode(
                    project,
                    textFunction = resultFunction,
                    appendTo = targetFileName,
                    sourceClass,
                    targetClass
                )
            } finally {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Kotlin Data Mapper")
                    .createNotification("Mapping function generated", NotificationType.INFORMATION)
                    .notify(project)
            }
        } else {
            val notFoundClass =
                if (sourceClass == null) sourceClassName
                else targetClassName

            NotificationGroupManager.getInstance()
                .getNotificationGroup("Kotlin Data Mapper")
                .createNotification(
                    "Generating failed",
                    "The \'$notFoundClass\' class was not found",
                    NotificationType.ERROR
                )
                .notify(project)
        }
    }

    private suspend fun generateAsExtensionFunction(
        project: Project, sourceClass: PsiClass, targetClass: PsiClass, extPattern: String
    ): String {
        val sb = StringBuilder()
        val bakedPattern = extPattern
            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, sourceClass.name!!)
            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, targetClass.name!!)

        sb.append("fun ${sourceClass.name}.$bakedPattern(): ${targetClass.name} { return ${targetClass.name}(")
        walkThroughSourceClassFieldsTree(project, sb, sourceClass, targetClass, "")
        sb.append(")}")
        return sb.toString()
    }

    private suspend fun generateAsGlobalFunction(
        project: Project, sourceClass: PsiClass, targetClass: PsiClass, globalPattern: String
    ): String {
        val sb = StringBuilder()
        val bakedPattern = globalPattern
            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, sourceClass.name!!)
            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, targetClass.name!!)

        sb.append("fun $bakedPattern(${sourceClass.name?.decapitalize()}: ${sourceClass.name}): ${targetClass.name} { return ${targetClass.name}(")
        walkThroughSourceClassFieldsTree(project, sb, sourceClass, targetClass, "")
        sb.append(")}")
        return sb.toString()
    }

    private suspend fun appendGeneratedCode(
        project: Project,
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
        sb: StringBuilder,
        sourceClass: PsiClass,
        targetClass: PsiClass,
        parentChainName: String,
    ) {
        writeCommandAction(project, "AddImport") {
            targetClass.kotlinFqName?.let { targetFile?.addImport(it, false, null, project) }
        }

        targetClass.fields.forEach { targetField ->
            val sourceField = sourceClass.fields.find { it.name == targetField.name }

            if (sourceField == null) {
                sb.append("${targetField.name} = null,")
            } else {
                val targetFieldType: PsiType = targetField.type
                val sourceFieldType: PsiType = sourceField.type

                if (targetFieldType.asPsiClass().isKotlinDataClass()) {
                    sb.append("${targetField.name} = ${targetFieldType.asPsiClass()?.name}(")
                    walkThroughSourceClassFieldsTree(
                        project,
                        sb,
                        sourceFieldType.asPsiClass()!!,
                        targetFieldType.asPsiClass()!!,
                        "$parentChainName.${targetField.name}",
                    )
                    sb.append(")" + ",")
                } else {
                    sb.append("${targetField.name} = ${prefix}${parentChainName}.${targetField.name}" + ",")
                }
            }
        }


//            else if (sourceField != null && sourceField.isKotlinListWithAnyParameterType()) {
//                val ktClass = sourceField.extractListParameterType()!!
//                val sourceClassName = ktClass.name!!
//                val targetClassName = ktClass.name!!.replace("DTO", "")
//                val bakedPattern = pattern
//                    .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, sourceClassName)
//                    .replace(FunctionNamePattern.TARGET_PLACEHOLDER, targetClassName)
//
//                sb.append("${targetField.name} = ${prefix}${parentChainName}.${targetField.name}.map($sourceClassName::$bakedPattern)" + ",")
//                MapperGenerator(
//                    project,
//                    MapperConfig(
//                        true,
//                        ktClass.containingFile.name,
//                        ktClass.fqName!!.asString(),
//                        "domain." + targetClassName
//                    )
//                ).generate(
//
//                )
//            }
    }

    private suspend fun findPsiClass(project: Project, className: String): PsiClass? = readAction {
        JavaPsiFacade.getInstance(project)
            .findClass(className, GlobalSearchScope.allScope(project))
    }

    private suspend fun findKtFileByName(project: Project, fileName: String?): KtFile? {
        fileName ?: return null

        val ktExtension = KotlinFileType.INSTANCE.defaultExtension
        val candidateFiles =
            readAction { FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.allScope(project)) }

        val ktFile: KtFile? = candidateFiles.find { virtualFile ->
            virtualFile.extension == ktExtension && virtualFile.toPsiFile(project) is KtFile
        }?.toPsiFile(project) as KtFile?

        return ktFile
    }
}
