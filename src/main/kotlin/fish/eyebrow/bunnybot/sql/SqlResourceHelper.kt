package fish.eyebrow.bunnybot.sql

import java.sql.Connection
import java.sql.ResultSet

fun Connection.queryUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): ResultSet {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeQuery(resourceData)
}

fun Connection.updateUsingResource(filePath: String, macroMap: Map<String, String> = emptyMap()): Int {
    val resourceData = collectFilePathData(filePath, macroMap)
    return createStatement().executeUpdate(resourceData)
}

private fun collectFilePathData(filePath: String, macroMap: Map<String, String>): String {
    return ClassLoader.getSystemResourceAsStream(filePath)!!.readAllBytes().map { it.toChar() }.joinToString("").apply {
        macroMap.forEach { entry ->
            replace(entry.key, entry.value)
        }
    }
}