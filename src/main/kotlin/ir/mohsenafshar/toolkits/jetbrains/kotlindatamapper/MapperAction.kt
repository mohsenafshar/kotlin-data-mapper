package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.BalloonBuilder
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import com.intellij.ui.BalloonImpl
import com.intellij.ui.GotItTooltip
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.data.AppSettings
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern
import org.jetbrains.kotlin.asJava.classes.KtLightClassForSourceDeclaration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.psi.imports.addImport
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import java.util.*


class MapperAction : AnAction() {

    private var sb = StringBuilder()
    private var prefix = "this"
    private var targetFile: KtFile? = null


    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val dialog = MapperInfoSelectionDialog(project, event)

        sb = StringBuilder()

        if (dialog.showAndGet()) {
            val (sourceClassName, targetClassName) = dialog.getSelectedClasses()
            val isExtensionFunction = dialog.isExtensionFunctionSelected()

            if (sourceClassName != null && targetClassName != null) {

                val sourceClass = findPsiClass(project, sourceClassName)
                val targetClass = findPsiClass(project, targetClassName)

                if (sourceClass != null && targetClass != null) {
                    prefix = if (isExtensionFunction) "this" else sourceClass.name!!.decapitalize()
                    targetFile = findKtFileByName(project, dialog.getSelectedFileName())
                        ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

                    val settings = AppSettings.instance.state
                    if (isExtensionFunction) {
                        val extPattern: String =
                            settings.userDefinedExtFunctionPattern ?: AppSettings.defaultExtPattern()
                        val bakedPattern = extPattern
                            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, sourceClass.name!!)
                            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, targetClass.name!!)

                        sb.append("fun ${sourceClass.name}.$bakedPattern(): ${targetClass.name} { return ${targetClass.name}(")
                        build(project, targetClass, sourceClass, "")
                        sb.append(")}")
                    } else {
                        val globalPattern: String =
                            settings.userDefinedGlobalFunctionPattern ?: AppSettings.defaultGlobalPattern()
                        val bakedPattern = globalPattern
                            .replace(FunctionNamePattern.SOURCE_PLACEHOLDER, sourceClass.name!!)
                            .replace(FunctionNamePattern.TARGET_PLACEHOLDER, targetClass.name!!)

                        sb.append("fun $bakedPattern(${sourceClass.name?.decapitalize()}: ${sourceClass.name}): ${targetClass.name} { return ${targetClass.name}(")
                        build(project, targetClass, sourceClass, "")
                        sb.append(")}")
                    }

                    try {
                        appendGeneratedCode(project, dialog.getSelectedFileName(), sourceClass, targetClass)
                    } finally {
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Kotlin Data Mapper")
                            .createNotification("Mapping function generated", NotificationType.INFORMATION)
                            .notify(project)
                    }
                } else {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Kotlin Data Mapper")
                        .createNotification("Generating failed","One of the classes ($sourceClassName or $targetClassName) was not found", NotificationType.ERROR)
                        .notify(project)
                }
            }
        }
    }

    private fun appendGeneratedCode(
        project: Project,
        selectedFileName: String?,
        sourceClass: PsiClass,
        targetClass: PsiClass
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            val psiFactory = KtPsiFactory(project)

            val containingFile = targetFile ?: findKtFileByName(project, selectedFileName)
            ?: (sourceClass as KtLightClassForSourceDeclaration).kotlinOrigin.containingKtFile

            containingFile.findDescendantOfType<KtFunction> {
                it.name?.contains("to${targetClass.name}") ?: false && it.receiverTypeReference?.text == sourceClass.name
            }?.apply {
                delete()
            }

            val newFunction = psiFactory.createFunction(sb.toString())
            containingFile.add(newFunction)
        }
    }

    private fun build(
        project: Project,
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
                    build(
                        project,
                        sourceField.type.asPsiClass()!!,
                        targetField.type.asPsiClass()!!,
                        "$parentChainName.${sourceField.name}",
                    )
                    sb.append(")")
                }
            } else {
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

    private fun PsiElement?.isKotlinDataClass(): Boolean {
        this ?: return false

        if (this is KtLightClassForSourceDeclaration) {
            return kotlinOrigin.isData()
        }
        return false
    }

    private fun PsiType.asPsiClass(): PsiClass? = PsiUtil.resolveClassInType(this)
}

fun String.decapitalize(): String = replaceFirstChar {
    it.lowercase(
        Locale.getDefault()
    )
}