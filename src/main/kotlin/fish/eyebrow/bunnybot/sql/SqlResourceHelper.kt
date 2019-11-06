package fish.eyebrow.bunnybot.sql

import java.sql.Connection
import java.sql.ResultSet

private val macroRegex = ":[A-z]{1,255}".toRegex()

fun Connection.queryUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): ResultSet {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeQuery(resourceData)
}

fun Connection.updateUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): Int {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeUpdate(resourceData)
}

private fun collectFilePathData(filePath: String, macroMap: Map<String, String>): String {
    var rawData = ClassLoader.getSystemResourceAsStream(filePath)!!.readAllBytes().map { it.toChar() }.joinToString("")
    macroMap.forEach { entry -> rawData = rawData.replace(entry.key, "'${entry.value}'") }
    return rawData.replace(macroRegex, "null")
}
