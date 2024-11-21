package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain

/*
*
* - For Extension functions: "asEntity" (fun UserDTO.asEntity(): User)
* - For Global functions: "map$SOURCE_CLASS$To$TARGET_CLASS$" (mapUserDTOToUser(userDTO: UserDTO): User)
* */
interface FunctionNamePattern {
    val sourceVariable: String
        get() = "\$SOURCE_CLASS\$"
    val targetVariable: String
        get() = "\$TARGET_CLASS\$"

    val defaultPattern: String
    val defaultPatternAsExpression: String
    val examplePattern: String
    val examplePatternExplanation: String
    val userDefinedPattern: String?

    fun buildFunctionSignatureFromPattern(): String
}

class ExtensionFunctionNamePattern : FunctionNamePattern {
    override val defaultPattern: String = "to${targetVariable}"
    override val defaultPatternAsExpression: String = "<html><span>Default &nbsp;&nbsp;: &nbsp;&nbsp;<b>to<i>${targetVariable}<i><b></span></html>"
    override val examplePattern: String = "asEntity"
    override val examplePatternExplanation: String =
        "<html><span>Example : &nbsp;&nbsp;<b>$examplePattern</b> | which will be resulted to: TargetClass.<b>$examplePattern</b>(): SourceClass{}</span></html>"
    override val userDefinedPattern: String? = null

    override fun buildFunctionSignatureFromPattern(): String {
        TODO("Not yet implemented")
    }
}

class GlobalFunctionNamePattern(
) : FunctionNamePattern {
    override val defaultPattern: String = "map${sourceVariable}To${targetVariable}"
    override val defaultPatternAsExpression: String = "<html><span>Default &nbsp;&nbsp;: &nbsp;&nbsp;<b>map<i>${sourceVariable}</i>To<i>${targetVariable}</i></b></span></html>"
    override val examplePattern: String = "\${${sourceVariable}.decapitalize()}As${targetVariable}"
    override val examplePatternExplanation: String = "..."
    override val userDefinedPattern: String? = null
    override fun buildFunctionSignatureFromPattern(): String {
        TODO("Not yet implemented")
    }
}