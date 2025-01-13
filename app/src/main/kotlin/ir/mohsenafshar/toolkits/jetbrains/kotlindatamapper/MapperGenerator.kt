package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
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

class MapperGenerator(private val project: Project) {
    private var targetFile: KtFile? = null
    private var prefix = "this"

    fun generate(
        isExtensionFunction: Boolean,
        targetFileName: String,
        sourceClassName: String,
        targetClassName: String
    ) {
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
                generateAsExtensionFunction(project, sourceClass, targetClass, extPattern)
            } else {
                val globalPattern: String =
                    settings.userDefinedGlobalFunctionPattern ?: AppSettings.defaultGlobalPattern()
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

    private fun generateAsExtensionFunction(
        project: Project, sourceClass: PsiClass, targetClass: PsiClass, extPattern: String
    ): String {
        val sb = StringBuilder()
        val bakedPattern = extPattern
            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, sourceClass.name!!)
            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, targetClass.name!!)

        sb.append("fun ${sourceClass.name}.$bakedPattern(): ${targetClass.name} { return ${targetClass.name}(")
        walkThroughSourceClassFieldsTree(project, sb, targetClass, sourceClass, "")
        sb.append(")}")
        return sb.toString()
    }

    private fun generateAsGlobalFunction(
        project: Project, sourceClass: PsiClass, targetClass: PsiClass, globalPattern: String
    ): String {
        val sb = StringBuilder()
        val bakedPattern = globalPattern
            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, sourceClass.name!!)
            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, targetClass.name!!)

        sb.append("fun $bakedPattern(${sourceClass.name?.decapitalize()}: ${sourceClass.name}): ${targetClass.name} { return ${targetClass.name}(")
        walkThroughSourceClassFieldsTree(project, sb, targetClass, sourceClass, "")
        sb.append(")}")
        return sb.toString()
    }

    private fun appendGeneratedCode(
        project: Project,
        textFunction: String,
        appendTo: String?,
        sourceClass: PsiClass,
        targetClass: PsiClass
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            val psiFactory = KtPsiFactory(project)

            val containingFile = targetFile ?: findKtFileByName(project, appendTo)
            ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

            containingFile.findDescendantOfType<KtFunction> {
                it.name?.contains("to${targetClass.name}") ?: false && it.receiverTypeReference?.text == sourceClass.name
            }?.apply {
                delete()
            }

            val newFunction = psiFactory.createFunction(textFunction)
            containingFile.add(newFunction)
        }
    }

    private fun walkThroughSourceClassFieldsTree(
        project: Project,
        sb: StringBuilder,
        sourceClass: PsiClass,
        targetClass: PsiClass,
        parentChainName: String,
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            sourceClass.kotlinFqName?.let { targetFile?.addImport(it, false, null, project) }
        }

        sourceClass.fields.forEach { sourceField ->
            val targetField = targetClass.fields.find { it.name == sourceField.name }

            if (sourceField.type.asPsiClass().isKotlinDataClass()) {
                if (targetField == null || targetField.type.asPsiClass().isKotlinDataClass().not()) {
                    sb.append("${sourceField.name} = null,")
                } else {
                    sb.append("${sourceField.name} = ${sourceField.type.asPsiClass()?.name}(")
                    walkThroughSourceClassFieldsTree(
                        project,
                        sb,
                        sourceField.type.asPsiClass()!!,
                        targetField.type.asPsiClass()!!,
                        "$parentChainName.${sourceField.name}",
                    )
                    sb.append(")")
                }
            }
//            else if(sourceField.type.asPsiClass().typeParameterList) {
//
//            }
            else {
                if (targetField == null) {
                    sb.append("${sourceField.name} = null,")
                } else {
                    sb.append("${sourceField.name} = ${prefix}${parentChainName}.${sourceField.name}" + ",")
                }
            }

        }
    }

    private fun findPsiClass(project: Project, className: String): PsiClass? {
        return JavaPsiFacade.getInstance(project)
            .findClass(className, GlobalSearchScope.allScope(project))
    }

    private fun findKtFileByName(project: Project, fileName: String?): KtFile? {
        fileName ?: return null

        val ktExtension = KotlinFileType.INSTANCE.defaultExtension
        val ktFile: KtFile? =
            FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScope.allScope(project)).find { virtualFile ->
                virtualFile.extension == ktExtension && virtualFile.toPsiFile(project) is KtFile
            }?.toPsiFile(project) as KtFile?

        return ktFile
    }
}
