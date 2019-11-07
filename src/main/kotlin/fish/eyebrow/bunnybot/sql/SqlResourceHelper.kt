package fish.eyebrow.bunnybot.sql

import java.sql.Connection
import java.sql.ResultSet

private const val NULL_VALUE = "null"
private val macroRegex = ":\\([A-z]{1,255}\\)".toRegex()

fun Connection.queryUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): ResultSet {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeQuery(resourceData)
}

fun Connection.updateUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): Int {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeUpdate(resourceData)
}

private fun collectFilePathData(filePath: String, macroMap: Map<String, String>): String {
    val macros = mutableListOf<String>()
    var mutableRawData = ClassLoader.getSystemResourceAsStream(filePath)!!.readAllBytes().map { it.toChar() }.joinToString("")
    for (l in mutableRawData.indices) {
        for (i in l until mutableRawData.length) {
            val substring = mutableRawData.substring(l, i)
            if (substring.matches(macroRegex) && !macros.contains(substring)) macros += substring
        }
    }
    for (macro in macros) {
        val replacement = macroMap[macro]?.let { "'$it'" } ?: NULL_VALUE
        mutableRawData = mutableRawData.replace(macro, replacement)
    }
    return mutableRawData
}
