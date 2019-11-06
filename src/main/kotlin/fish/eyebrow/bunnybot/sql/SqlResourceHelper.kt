package fish.eyebrow.bunnybot.sql

import java.sql.Connection
import java.sql.ResultSet

private const val MACRO_STRING = ":[A-z]{1,255}"
private val spaceRegex = " ".toRegex()

fun Connection.queryUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): ResultSet {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeQuery(resourceData)
}

fun Connection.updateUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): Int {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeUpdate(resourceData)
}

private fun collectFilePathData(filePath: String, macroMap: Map<String, String>): String {
    val addedForExclusion = mutableListOf<String>()
    var rawData = ClassLoader.getSystemResourceAsStream(filePath)!!.readAllBytes().map { it.toChar() }.joinToString("")
    macroMap.forEach { entry ->
        rawData = rawData.replace(entry.key, "'${entry.value}'")
        addedForExclusion += "(?!${entry.value.replace(spaceRegex, "|")}.*$)"
    }
    val cleanUpRegex = "${addedForExclusion.joinToString("")}$MACRO_STRING".toRegex()
    return rawData.replace(cleanUpRegex, "null")
}
