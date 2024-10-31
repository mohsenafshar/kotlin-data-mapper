package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*
import com.intellij.psi.PsiManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade

class NewClassSelectionDialog(private val project: Project) : DialogWrapper(project) {
    private val sourceClassField = JBTextField()
    private val targetClassField = JBTextField()
    private val extensionFunctionRadio = JRadioButton("Extension Function", true)
    private val globalFunctionRadio = JRadioButton("Global Function")
    private val generateSeparateFileCheckbox = JCheckBox("Generate in a separate file")
    private val fileNameField = JBTextField()
    private val fileNameLabel = JLabel("File Name:", SwingConstants.LEFT)

    init {
        title = "Select Classes and File Options for Mapping"
        init()

        // Hide fileNameField initially
        fileNameField.isVisible = false

        // Checkbox action to toggle fileNameField visibility
        generateSeparateFileCheckbox.addItemListener {
            fileNameField.isVisible = generateSeparateFileCheckbox.isSelected
            if (generateSeparateFileCheckbox.isSelected) {
                // Set default file name based on the source class
                fileNameField.text = sourceClassField.text.takeIf { it.isNotEmpty() } ?: "GeneratedMapper"
            }
            this.window.pack()  // Adjust the dialog size
        }
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        // Source Class Selection Panel
        val sourcePanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Source Class (DTO):", SwingConstants.LEFT), BorderLayout.WEST)
            add(sourceClassField, BorderLayout.CENTER)
            add(createSelectClassButton(sourceClassField, "Select Source Class (DTO)"), BorderLayout.EAST)
        }

        // Target Class Selection Panel
        val targetPanel = JPanel(BorderLayout()).apply {
            add(JBLabel("Target Class (Domain):", SwingConstants.LEFT), BorderLayout.WEST)
            add(targetClassField, BorderLayout.CENTER)
            add(createSelectClassButton(targetClassField, "Select Target Class (Domain)"), BorderLayout.EAST)
        }

        // Function Type Selection Panel
        val functionTypePanel = JPanel().apply {
            border = BorderFactory.createTitledBorder("Function Type")
            add(extensionFunctionRadio)
            add(globalFunctionRadio)
        }

        // ButtonGroup for mutual exclusivity
        ButtonGroup().apply {
            add(extensionFunctionRadio)
            add(globalFunctionRadio)
        }

        // File Option Panel with Checkbox and optional File Name field
        val fileOptionPanel = JPanel(BorderLayout()).apply {
            add(generateSeparateFileCheckbox, BorderLayout.NORTH)

            // Row for File Name input, visible when checkbox is selected
            val fileNamePanel = JPanel(BorderLayout()).apply {
                add(fileNameLabel, BorderLayout.WEST)
                fileNameLabel.isVisible = false
                add(fileNameField, BorderLayout.CENTER)
                fileNameField.isVisible = false // Initially hidden
            }
            add(fileNamePanel, BorderLayout.SOUTH)
        }

        // Checkbox action to toggle visibility of fileNameField
        generateSeparateFileCheckbox.addItemListener {
            fileNameLabel.isVisible = generateSeparateFileCheckbox.isSelected
            fileNameField.isVisible = generateSeparateFileCheckbox.isSelected
            if (generateSeparateFileCheckbox.isSelected) {
                // Set default file name based on the source class
                fileNameField.text = "GeneratedMapper"
//                fileNameField.text = sourceClassField.text.takeIf { it.isNotEmpty() } ?: "GeneratedMapper"
            }
            this.window.pack()  // Adjust dialog size
        }

        // Add all components to the main panel
        mainPanel.add(sourcePanel)
        mainPanel.add(targetPanel)
        mainPanel.add(functionTypePanel)
        mainPanel.add(fileOptionPanel)

        return mainPanel
    }

    private fun createSelectClassButton(classField: JBTextField, dialogTitle: String): JButton {
        return JButton("...").apply {
            addActionListener {
                val classChooser = TreeClassChooserFactory.getInstance(project).createAllProjectScopeChooser(dialogTitle)
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

    fun getSelectedClasses(): Pair<String?, String?> {
        return Pair(sourceClassField.text, targetClassField.text)
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
            val psiClass = JavaPsiFacade.getInstance(project).findClass(sourceClassName, GlobalSearchScope.projectScope(project))
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
