package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.base.psi.kotlinFqName
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType
import java.awt.*
import javax.swing.*


class NewClassSelectionDialog(private val project: Project, event: AnActionEvent) : DialogWrapper(project) {
    private val sourceClassField = JBTextField().apply {
        text = event.getData(CommonDataKeys.PSI_FILE)?.findDescendantOfType<KtClass>()?.kotlinFqName?.asString() ?: ""
    }
    private val targetClassField = JBTextField()
    private val targetFileField = JBTextField().apply {
        text = event.getData(CommonDataKeys.PSI_FILE)?.name ?: ""
    }
    private val extensionFunctionRadio = JRadioButton("Extension Function", true)
    private val globalFunctionRadio = JRadioButton("Global Function")
    private val generateSeparateFileCheckbox = JCheckBox("Generate in a separate file")
    private val fileNameField = JBTextField().apply { isEnabled = false }
    private val fileNameLabel = JLabel("File Name:", SwingConstants.LEFT)

    init {
        title = "Generate Mapping Function"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        // Source Class Selection Panel
        val sourcePanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Source Class : ", SwingConstants.LEFT), BorderLayout.WEST)
            add(sourceClassField, BorderLayout.CENTER)
            add(createSelectClassButton(sourceClassField, "Select Source Class"), BorderLayout.EAST)
        }

        // Target Class Selection Panel
        val targetPanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Target Class :  ", SwingConstants.LEFT), BorderLayout.WEST)
            add(targetClassField, BorderLayout.CENTER)
            add(createSelectClassButton(targetClassField, "Select Target Class"), BorderLayout.EAST)
        }

        // Target Class Selection Panel
        val targetFilePanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Generate in :   ", SwingConstants.LEFT), BorderLayout.WEST)
            add(targetFileField, BorderLayout.CENTER)
            add(createSelectFileButton(targetFileField, "Select Mapper Function File"), BorderLayout.EAST)
        }

        // Function Type Selection Panel
        val functionTypePanel = JPanel().apply {
//            val roundedLineBorder = LineBorder(JBColor(Color.LIGHT_GRAY, Color.DARK_GRAY), 2, true)
            border = BorderFactory.createTitledBorder("Function Type").apply { titleColor = JBColor(Color.BLACK, Color.WHITE) }
            add(extensionFunctionRadio)
            add(globalFunctionRadio)
        }

        // ButtonGroup for mutual exclusivity
        ButtonGroup().apply {
            add(extensionFunctionRadio)
            add(globalFunctionRadio)
        }

        // Add all components to the main panel
        mainPanel.add(sourcePanel)
        mainPanel.add(targetPanel)
        mainPanel.add(targetFilePanel)
        mainPanel.add(Box.createVerticalStrut(8))
        mainPanel.add(functionTypePanel)
//        mainPanel.add(getGenerateInNewFileCheckboxPanel())

        return mainPanel
    }

    private fun getGenerateInNewFileCheckboxPanel(): JPanel {
        val fileOptionPanel = JPanel(BorderLayout()).apply {
            add(generateSeparateFileCheckbox, BorderLayout.NORTH)

            val fileNamePanel = JPanel(BorderLayout()).apply {
                add(fileNameLabel, BorderLayout.WEST)
                fileNameLabel.isEnabled = false
                add(fileNameField, BorderLayout.CENTER)
                fileNameField.isEnabled = false // Initially hidden
            }
            add(fileNamePanel, BorderLayout.SOUTH)
        }

        generateSeparateFileCheckbox.addItemListener {
            fileNameLabel.isEnabled = generateSeparateFileCheckbox.isSelected
            fileNameField.isEnabled = generateSeparateFileCheckbox.isSelected
            if (generateSeparateFileCheckbox.isSelected) {
                fileNameField.text = "GeneratedMapper"
            }

            this.window.pack()  // Adjust dialog size
        }

        return fileOptionPanel
    }

    private fun createSelectClassButton(classField: JBTextField, dialogTitle: String): JButton {
        return JButton("...").apply {
            addActionListener {
                val classChooser = TreeClassChooserFactory.getInstance(project)
                    .createAllProjectScopeChooser(dialogTitle) // todo: Search for kotlin data classes only
                classChooser.showDialog()
                val selectedClass = classChooser.selected
                classField.text = selectedClass?.qualifiedName ?: ""

                // Set default file name if checkbox is checked and source class is selected
                if (generateSeparateFileCheckbox.isSelected && classField == sourceClassField) {
                    fileNameField.text = selectedClass?.name ?: "GeneratedMapper"
                }
            }
        }
    }

    private fun createSelectFileButton(fileField: JBTextField, dialogTitle: String): JButton {
        return JButton("...").apply {
            addActionListener {
//                val fileDesc = FileChooserDescriptor(true, false, false, false, false, false)
//                FileChooser.chooseFile(fileDesc, project, null) { consumer: VirtualFile ->
//                    println(consumer.name)
//                    classField.text = consumer.name ?: ""
//                }

                val fileChooser = TreeFileChooserFactory.getInstance(project).createFileChooser(
                    dialogTitle, null,
                    KotlinFileType.INSTANCE, null,
                )
                fileChooser.showDialog()
                val selectedFile = fileChooser.selectedFile
                fileField.text = selectedFile?.name ?: ""
            }
        }
    }

    fun getSelectedClasses(): Pair<String?, String?> {
        return Pair(sourceClassField.text, targetClassField.text)
    }

    fun getSelectedFileName(): String? {
        return targetFileField.text
    }

    fun isExtensionFunctionSelected(): Boolean {
        return extensionFunctionRadio.isSelected
    }

    fun isSeparateFileGenerationEnabled(): Boolean {
        return generateSeparateFileCheckbox.isSelected
    }

    fun getFileName(): String? {
        return if (isSeparateFileGenerationEnabled()) fileNameField.text else null
    }

    // Function to get the Document for the selected source class
    fun getSourceClassDocument(): Document? {
        val sourceClassName = sourceClassField.text
        if (sourceClassName.isNotEmpty()) {
            // Find the PsiClass for the source class name
            val psiClass =
                JavaPsiFacade.getInstance(project).findClass(sourceClassName, GlobalSearchScope.projectScope(project))
            val psiFile = psiClass?.containingFile

            if (psiFile != null) {
                // Get the VirtualFile and then the Document
                val virtualFile: VirtualFile? = psiFile.virtualFile
                return virtualFile?.let { FileDocumentManager.getInstance().getDocument(it) }
            }
        }
        return null
    }
}

class RoundedPanel(private val borderColor: Color, private val radius: Int) : JPanel() {
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = borderColor
        g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius) // Use the radius here
    }
}
