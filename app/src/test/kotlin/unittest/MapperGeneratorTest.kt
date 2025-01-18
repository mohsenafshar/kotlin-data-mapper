package unittest

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import ir.mohsenafshar.toolkits.jetbrains.ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.models.MapperClassInfoHolder
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.MapperGenerator
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.data.AppSettings
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.findDescendantOfType

fun KtFile.findExtensionFunctions(): List<KtNamedFunction> {
    val functions = PsiTreeUtil.collectElementsOfType(this, KtNamedFunction::class.java)
    return functions.filter { it.receiverTypeReference != null }
}

fun KtFile.findExtensionFunction(funName: String, receiverName: String): KtFunction? {
    return findDescendantOfType<KtFunction> {
        it.name?.contains(funName) == true && it.receiverTypeReference?.text == receiverName
    }
}

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MapperGeneratorTest : BasePlatformTestCase() {

    fun testGeneratingMapperFunctionBasedOnClassesInSameFile() = runBlocking {
        myFixture.testDataPath = "$testDataPath/aio"
        val psiFiles = myFixture.configureByFiles("UserDTOAndDependencies.kt", "UserAndDependencies.kt")
        val sourceKtFile = assertInstanceOf(psiFiles.first(), KtFile::class.java)
        val targetKtFile = assertInstanceOf(psiFiles[1], KtFile::class.java)

        val sourceKtClasses = sourceKtFile.findChildrenByClass<KtClass>(KtClass::class.java)
        val sourceKtClass = sourceKtClasses.find { ktClass -> ktClass.name == "UserDTO" }!!
        assertNotNull(sourceKtClass)

        val targetKtClasses = targetKtFile.findChildrenByClass<KtClass>(KtClass::class.java)
        val targetKtClass = targetKtClasses.find { ktClass -> ktClass.name == "User" }!!
        assertNotNull(targetKtClass)

        val config = MapperGenerator.Config(
            isExtensionFunction = true,
            destinationFileName = targetKtFile.name,
            sourceClassName = sourceKtClass.name!!,
            targetClassName = targetKtClass.name!!,
        )
        val classInfoHolder = MapperClassInfoHolder(
            sourceKtClass.toLightClass()!!, sourceKtClass.name!!.split(".").last(),
            targetKtClass.toLightClass()!!, targetKtClass.name!!.split(".").last(),
            pattern = AppSettings.defaultExtPattern()
        )
        val result = MapperGenerator(project, config).generateAsExtensionFunction(classInfoHolder)

        println(result)
        assert(result.isNotEmpty())
    }

    fun testGeneratingMapperBasedOnClassesInSamePackageDifferentFiles() {
        myFixture.testDataPath = "$testDataPath/same-package"
        val psiFiles = myFixture.configureByFiles("AddressDTO", "HobbyDTO", "UserDTO")


    }


    override fun getTestDataPath() = "src/test/testData/mapper/generate"
}