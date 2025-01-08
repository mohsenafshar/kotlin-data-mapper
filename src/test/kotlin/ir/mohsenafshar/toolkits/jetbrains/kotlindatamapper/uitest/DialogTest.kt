package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.uitest

import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.openFile
import com.intellij.driver.sdk.ui.components.dialog
import com.intellij.driver.sdk.ui.components.ideFrame
import com.intellij.driver.sdk.ui.components.welcomeScreen
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.junit5.hyphenateWithClass
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.runner.CurrentTestMethod
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes

class DialogTest {

    @Test
    fun checkDialogGenerateButtonInitialState_isDisabled() {
        val projectDir = Path("H:\\Project\\java\\data-mapper\\DataMapper")
        val localProjectInfo = LocalProjectInfo(projectDir)

        Starter
            .newContext(
                CurrentTestMethod.hyphenateWithClass(), TestCase(
                    IdeProductProvider.IC, localProjectInfo
                ).withVersion("2024.2.4")
            )
            .apply {
                val pathToPlugin = "build/distributions/KotlinDataMapper-0.3.0.zip" //System.getProperty()
                PluginConfigurator(this).installPluginFromPath(Path(pathToPlugin))
                this.applyVMOptionsPatch {
                    addSystemProperty("-Dcom.sun.management.jmxremote.port", "7777")
                }
            }.runIdeWithDriver().useDriverAndCloseIde {
                waitForIndicators(1.minutes)

                openFile("src/main/kotlin/ir/mohsenafshar/toolkits/jetbrains/kotlindatamapper/Test.kt")

                invokeAction("GenerateMapperAction", now = false)

                ideFrame {
                    dialog(xpath = "//div[@class='MyDialog']") {
                        assert(
                            x {
                                byText("Generate")
                            }.isEnabled().not()
                        )
                    }
                }
            }
    }

    @Test
    fun checkPluginInstalled() {
        Starter
            .newContext(
                CurrentTestMethod.hyphenateWithClass(), TestCase(
                    IdeProductProvider.IC, NoProject
                ).withVersion("2024.2.4")
            )
            .apply {
                val pathToPlugin = "build/distributions/KotlinDataMapper-0.3.0.zip" //System.getProperty()
                PluginConfigurator(this).installPluginFromPath(Path(pathToPlugin))
            }.runIdeWithDriver().useDriverAndCloseIde {
                welcomeScreen {
                    clickPlugins()
                    x { byAccessibleName("Installed") }.click()
                    shouldBe("Plugin is installed") {
                        x {
                            and(
                                byVisibleText("Kotlin Data Mapper"),
                                byJavaClass("javax.swing.JLabel")
                            )
                        }.present()
                    }
                }
            }
    }
}