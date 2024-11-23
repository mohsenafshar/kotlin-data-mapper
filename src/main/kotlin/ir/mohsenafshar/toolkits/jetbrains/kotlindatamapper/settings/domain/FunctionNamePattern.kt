package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain

import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern.Companion.SOURCE_PLACEHOLDER
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern.Companion.TARGET_PLACEHOLDER


interface FunctionNamePattern {

    companion object {
        const val SOURCE_PLACEHOLDER = "\$SOURCE\$"
        const val TARGET_PLACEHOLDER = "\$TARGET\$"
    }

    fun defaultPattern(): String
    fun defaultPatternAsHtml(): String

    fun validate(userInputPattern: String?): Boolean {
        userInputPattern ?: return false

        return userInputPattern.isNotBlank()
    }
}

class ExtensionFunctionNamePattern : FunctionNamePattern {
    override fun defaultPattern(): String = "to${TARGET_PLACEHOLDER}"
    override fun defaultPatternAsHtml(): String =
        "<html><span>Default &nbsp;&nbsp;: &nbsp;&nbsp;to<i>${TARGET_PLACEHOLDER}<i></span></html>"
}

class GlobalFunctionNamePattern : FunctionNamePattern {
    override fun defaultPattern(): String = "map${SOURCE_PLACEHOLDER}To${TARGET_PLACEHOLDER}"
    override fun defaultPatternAsHtml(): String =
        "<html><span>Default &nbsp;&nbsp;: &nbsp;&nbsp;map<i>${SOURCE_PLACEHOLDER}</i>To<i>${TARGET_PLACEHOLDER}</i></span></html>"
}