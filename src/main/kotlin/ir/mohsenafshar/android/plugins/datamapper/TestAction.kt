package ir.mohsenafshar.android.plugins.datamapper

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.kotlin.analysis.api.KaSession
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.symbols.KaNamedClassSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KaValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KaSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.symbol
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtTypeReference


//fun UserDTO.toUser(): User {
//    return User(
//        age = this.age,
//        fam = this.fam,
//        address = Address(
//            city = this.address.city,
//            apt = this.address.apt,
//        )
//    )
//}

// Kt

class TestAction : AnAction() {

    var sb = StringBuilder()

    fun KaSession.build(paramterPointer: KaSymbolPointer<KaValueParameterSymbol>, parentChainName: String) {
        val paramter = paramterPointer.restoreSymbol()!!

        sb.append("${paramter.name} = ")

        if (paramter.isDataClass1()) {
            val ktClass = paramter.returnType.symbol?.psi as KtClass

            sb.append("${ktClass.name}(")

            val parameters: List<KaValueParameterSymbol> = getKaValueParameterSymbolFromKtClass(ktClass) ?: emptyList()
            parameters.forEach {
                build(it.createPointer(), parentChainName + "." + it.name)
            }

            sb.append(")")
        } else {
            sb.append("this${parentChainName}.${paramter.name}" + ",") // todo: handle parameter.name as null string
        }
    }

    private fun getKaValueParameterSymbolFromKtClass(ktClass: KtClass): List<KaValueParameterSymbol>? {
        analyze(ktClass) {
            return ktClass.primaryConstructor?.symbol?.valueParameters
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        CoroutineScope(Dispatchers.Default).launch {
//            val psiFile = e.getData(CommonDataKeys.PSI_FILE)
//            val editor = e.getData(CommonDataKeys.EDITOR)
//            println(psiFile?.name)
//
//            psiFile ?: throw Exception("PSI File not found")
//
//            val classes = psiFile.getChildrenOfType<KtClass>().asList()

            readAction {
                try {
                    val userDTO = FilenameIndex.getAllFilenames(e.project!!).filter { it.contains("UserDTO") }
                    println(userDTO)

                    val vfList =
                        FilenameIndex.getVirtualFilesByName(userDTO.first(), GlobalSearchScope.allScope(e.project!!))

                    val psiFile: PsiFile? = PsiManager.getInstance(e.project!!).findFile(vfList.first())
                    psiFile!!.accept(object : PsiRecursiveElementWalkingVisitor() {
                        override fun visitElement(element: PsiElement) {
                            super.visitElement(element)
                            if (element is KtClass) {
                                println(element.name)

                                if (element.name == "UserDTO") {
                                    analyze(element) {
                                        val constructorSymbol = element.primaryConstructor?.symbol ?: return@analyze
                                        sb.append("fun UserDTO.toUser(): User { return User(")
                                        constructorSymbol.valueParameters.forEach { build(it.createPointer(), "") }
                                        sb.append(")}")
                                    }
                                }
                            }
                        }

                    })
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            println(sb.toString())
        }
    }

    private fun findTargetClassOnTargetEntityByParameterName(name: String): KtClass {
        TODO("Not yet implemented")
    }

    private fun getUserPackageName(psiFile: PsiFile): String? {
        return (psiFile as? KtFile)?.packageFqName?.asString()
    }

    private fun isCustomType(psiType: PsiType, userPackageName: String?): Boolean {
        val resolvedClass = PsiUtil.resolveClassInType(psiType)
        val classPackage = resolvedClass?.qualifiedName?.substringBeforeLast('.')
        return classPackage == null || classPackage == "" || classPackage == userPackageName
    }

    fun findCustomTypesInDto(dtoClass: KtClass, psiFile: PsiFile) {
        val userPackageName = getUserPackageName(psiFile)

//        dtoClass.getProperties().forEach { property ->
        dtoClass.primaryConstructorParameters.forEach { property ->
            val propertyType = property.typeReference?.text ?: return@forEach
            val psiType =
                PsiType.getTypeByName(propertyType, dtoClass.project, GlobalSearchScope.allScope(dtoClass.project))

            if (isCustomType(psiType, userPackageName)) {
                println("${property.name} is a custom type.")
            } else {
                println("${property.name} is a primitive type.")
            }
        }
    }

    // Function to get the package name of a property's type
    private fun getPackageNameOfPropertyType(typeReference: KtTypeReference): String? {
        // Analyze the type reference in the Analysis API context
        return runReadAction {
            analyze(typeReference) {
                val type = typeReference.type
                val classSymbol = type.expandedSymbol
                classSymbol?.classId?.packageFqName?.asString()
            }
        }
    }

    suspend fun getPackageNameOfPropertyTypeAsync(typeReference: KtTypeReference): String? {
        // Switch to a background thread to run the analysis
        return withReadActionDefaultContext {
            analyze(typeReference) {
                val type = typeReference.type
                val classSymbol = type.expandedSymbol
                classSymbol?.classId?.packageFqName?.asString()
            }
        }
    }

    // Function to find and print package names of all properties in the DTO class
    fun printPropertyPackageNames(dtoClass: KtClass) {
        val constructorParameters = dtoClass.primaryConstructorParameters

        dtoClass.isData()

        CoroutineScope(Dispatchers.Main).launch {
            constructorParameters.forEach { parameter: KtParameter ->
                val propertyTypeRef = parameter.typeReference ?: return@forEach
                val packageName = getPackageNameOfPropertyTypeAsync(propertyTypeRef)
                println("Property '${parameter.name}': Package = $packageName")
            }
        }
    }

    suspend fun checkParametersAreDataClasses(userDtoClass: KtClass) {
        withReadActionDefaultContext {
            analyze(userDtoClass) {
                val constructorSymbol = userDtoClass.primaryConstructor?.symbol ?: return@analyze

                // Iterate over parameters of the primary constructor
                for (parameter in constructorSymbol.valueParameters) {
                    checkIsDataClass(parameter)
                }
            }
        }
    }

    fun checkIsDataClass(parameter: KaValueParameterSymbol): Boolean {
        val parameterType = parameter.returnType
        val parameterSymbol = parameterType.symbol

        if (parameterSymbol is KaNamedClassSymbol) {
            // Check if the parameter type is a data class
            if (parameterSymbol.isData) {
                println("${parameter.name} is a data class")
                return true
            } else {
                println("${parameter.name} is NOT a data class")
                return true
            }
        } else {
            println("${parameter.name} is NOT a data class (not a class type)")
            return false
        }
    }

    private suspend fun generateMappingCode(
        sourceClass: KtClass,
        targetClass: KtClass,
        isExtensionFunction: Boolean
    ): String {
        var res = ""
        withReadActionDefaultContext {
            val mappings = targetClass.primaryConstructorParameters.joinToString(",\n") { parameter: KtParameter ->
                analyze(parameter) {
                    val constructorSymbol = targetClass.primaryConstructor?.symbol ?: return@analyze ""

                    // Iterate over parameters of the primary constructor
                    for (parameter in constructorSymbol.valueParameters) {
                        checkIsDataClass(parameter)
                    }

                    return@analyze ""
                }
            }

            res = mappings
        }
        return res
    }
}

fun KaValueParameterSymbol.isDataClass1(): Boolean {
    val parameterSymbol = returnType.symbol
    return if (parameterSymbol is KaNamedClassSymbol) {
        parameterSymbol.isData
    } else {
        false
    }
}

suspend fun <T> withReadActionDefaultContext(block: () -> T): T {
    return withContext(Dispatchers.Default) {
        runReadAction {
            block()
        }
    }
}