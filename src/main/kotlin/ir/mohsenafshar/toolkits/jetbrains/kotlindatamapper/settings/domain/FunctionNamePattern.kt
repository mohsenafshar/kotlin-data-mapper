package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain

import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern.Companion.SOURCE_CLASS_PLACEHOLDER
import ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.settings.domain.FunctionNamePattern.Companion.TARGET_CLASS_PLACEHOLDER


interface FunctionNamePattern {

    companion object {
        const val SOURCE_CLASS_PLACEHOLDER = "\$SOURCE\$"
        const val TARGET_CLASS_PLACEHOLDER = "\$TARGET\$"
    }

    fun defaultPattern(): String
    fun defaultPatternAsHtml(): String

    fun validate(userInputPattern: String?): Boolean {
        userInputPattern ?: return false

        return userInputPattern.isNotBlank()
    }
}

class ExtensionFunctionNamePattern : FunctionNamePattern {
    override fun defaultPattern(): String = "to${TARGET_CLASS_PLACEHOLDER}"
    override fun defaultPatternAsHtml(): String =
        "<html><span>Default &nbsp;&nbsp;: &nbsp;&nbsp;to<i>${TARGET_CLASS_PLACEHOLDER}<i></span></html>"
}

class GlobalFunctionNamePattern : FunctionNamePattern {
    override fun defaultPattern(): String = "map${SOURCE_CLASS_PLACEHOLDER}To${TARGET_CLASS_PLACEHOLDER}"
    override fun defaultPatternAsHtml(): String =
        "<html><span>Default &nbsp;&nbsp;: &nbsp;&nbsp;map<i>${SOURCE_CLASS_PLACEHOLDER}</i>To<i>${TARGET_CLASS_PLACEHOLDER}</i></span></html>"
}