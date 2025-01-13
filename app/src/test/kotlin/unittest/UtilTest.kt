package unittest

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.isKotlinListWithParameterTypeOf
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.*


@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class UtilTest : BasePlatformTestCase() {

    fun testListType() = runBlocking {
        val file = myFixture.configureByFile("UserDTO.kt")
        val ktFile = assertInstanceOf(file, KtFile::class.java)
        val ktClasses: Collection<KtClass> = PsiTreeUtil.collectElementsOfType(file, KtClass::class.java)

        val psiClasses = ktClasses.mapNotNull { it.toLightClass() }

        val psiField = psiClasses[0].fields[1]
        assertNotNull("PsiField for 'list' should not be null", psiField)

        // Check if it's a List<> type
        assertTrue("Field 'list' should be a List type", psiField.isKotlinListWithParameterTypeOf("HobbyDTO"))
    }

    override fun getTestDataPath() = "src/test/testData/mapper"
}