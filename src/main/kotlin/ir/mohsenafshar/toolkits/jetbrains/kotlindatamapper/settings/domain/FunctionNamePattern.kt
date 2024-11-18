package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.models

/*
*
* - For Extension functions: "asEntity" (fun UserDTO.asEntity(): User)
* - For Global functions: "map$SOURCE_CLASS$To$TARGET_CLASS$" (mapUserDTOToUser(userDTO: UserDTO): User)
* */
interface FunctionNamePattern {
    val defaultPattern: String
    val examplePattern: String
    val userDefinedPattern: String?

    fun buildFunctionSignatureFromPattern(): String
}

class ExtensionFunctionNamePattern: FunctionNamePattern {
    override val defaultPattern: String = "to\$TARGET_CLASS$"
    override val examplePattern: String = "asEntity"
    override val userDefinedPattern: String? = null

    override fun buildFunctionSignatureFromPattern(): String {
        TODO("Not yet implemented")
    }
}

class GlobalFunctionNamePattern(
    override val defaultPattern: String,
    override val examplePattern: String,
    override val userDefinedPattern: String?
) : FunctionNamePattern {
    override fun buildFunctionSignatureFromPattern(): String {
        TODO("Not yet implemented")
    }
}