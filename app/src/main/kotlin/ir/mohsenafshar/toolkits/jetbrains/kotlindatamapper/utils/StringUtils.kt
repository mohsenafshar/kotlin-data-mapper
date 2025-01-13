package ir.mohsenafshar.toolkits.jetbrains.kotlindatamapper.utils

import java.util.Locale


fun String.asHtml(): String = "<html><body>$this</body></html>"

fun String.decapitalize(): String = replaceFirstChar {
    it.lowercase(
        Locale.getDefault()
    )
}