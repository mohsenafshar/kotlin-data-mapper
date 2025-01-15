package unittest

import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.extractListParameterType
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.findPsiClassByFQName
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.isKotlinListWithAnyParameterType
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils.isKotlinListWithParameterTypeOf
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.*


@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class UtilTest : BasePlatformTestCase() {

    fun testFindPsiClassByFullyQualifiedName() = runBlocking {
        val file = myFixture.configureByFile("UserDTO.kt")
        val ktFile = assertInstanceOf(file, KtFile::class.java)
        assertEquals("feature.data.model", ktFile.packageFqName.asString())

        val searchResult = findPsiClassByFQName(project, "${ktFile.packageFqName.asString()}.UserDTO")
        assertNotNull(searchResult)
    }

    fun testFindPsiClassByShortStringName() = runBlocking {
        myFixture.configureByFile("UserDTO.kt")

        val allClasses: Array<PsiClass> = PsiShortNamesCache.getInstance(project).getClassesByName("UserDTO", GlobalSearchScope.allScope(project))
        assert(allClasses.isNotEmpty())
    }

    fun testListTypeWithSpecificParameterType() = runBlocking {
        val file = myFixture.configureByFile("UserDTO.kt")
        val ktFile = assertInstanceOf(file, KtFile::class.java)
        val ktClasses: Collection<KtClass> = PsiTreeUtil.collectElementsOfType(file, KtClass::class.java)

        val psiClasses = ktClasses.mapNotNull { it.toLightClass() }

        val psiField = psiClasses[0].fields[1]
        assertNotNull("PsiField for 'list' should not be null", psiField)

        // Check if it's a List<> type
        assertTrue("Field 'list' should be a List type", psiField.isKotlinListWithParameterTypeOf("HobbyDTO"))
    }

    fun testListTypeWithUnspecifiedParameterType() {
        val file = myFixture.configureByFile("UserDTO.kt")
        val ktFile = assertInstanceOf(file, KtFile::class.java)
        val ktClasses: Collection<KtClass> = PsiTreeUtil.collectElementsOfType(file, KtClass::class.java)

        val psiClasses = ktClasses.mapNotNull { it.toLightClass() }

        val psiField = psiClasses[0].fields[1]
        assertNotNull("PsiField for 'list' should not be null", psiField)

        // Check if it's a List<> type
        assertTrue("Field 'list' should be a List type", psiField.isKotlinListWithAnyParameterType())
    }

    fun testExtractParameterClassTypeFormPsiFieldWIthTypeOfList() = runBlocking {
        val file = myFixture.configureByFile("UserDTO.kt")

        val ktClasses: Collection<KtClass> = PsiTreeUtil.collectElementsOfType(file, KtClass::class.java)
        val psiClasses = ktClasses.mapNotNull { it.toLightClass() }
        val psiField = psiClasses[0].fields[1]

        val resultKtClass: KtClass? = psiField.extractListParameterType()
        assertNotNull(resultKtClass)

        assertEquals(resultKtClass!!.name, "HobbyDTO")
    }

    override fun getTestDataPath() = "src/test/testData/mapper"
}