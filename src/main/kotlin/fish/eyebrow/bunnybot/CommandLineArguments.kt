package fish.eyebrow.bunnybot

data class CommandLineArguments(val token: String? = null)

object CommandLineParser {
    private val tokenRegex: Regex = "(-token=).*".toRegex()

    fun parse(args: Array<String>): CommandLineArguments {
        return CommandLineArguments(
            token = getOptionValueWithRegexOrNull(args, tokenRegex)
        )
    }

    private fun getOptionValueWithRegexOrNull(args: Array<String>, regex: Regex) =
            args.findLast { it.matches(regex) }?.split("=")?.get(1)
}
