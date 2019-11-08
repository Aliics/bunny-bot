package fish.eyebrow.bunnybot.util

fun collectFilePathData(filePath: String): String =
        ClassLoader.getSystemResourceAsStream(filePath)!!.readAllBytes().map { it.toChar() }.joinToString("")
