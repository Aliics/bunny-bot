package fish.eyebrow.bunnybot

data class CommandLineArguments(
        val token: String? = null,
        val dbUrl: String? = null,
        val dbUser: String? = null,
        val dbPassword: String? = null
)

object CommandLineParser {
    private val tokenRegex: Regex = "(-token=).*".toRegex()
    private val dbUrlRegex: Regex = "(-db.url=).*".toRegex()
    private val dbUserRegex: Regex = "(-db.user=).*".toRegex()
    private val dbPasswordRegex: Regex = "(-db.password=).*".toRegex()

    fun parse(args: Array<String>): CommandLineArguments {
        return CommandLineArguments(
            token = getOptionValueWithRegexOrNull(args, tokenRegex),
            dbUrl = getOptionValueWithRegexOrNull(args, dbUrlRegex),
            dbUser = getOptionValueWithRegexOrNull(args, dbUserRegex),
            dbPassword = getOptionValueWithRegexOrNull(args, dbPasswordRegex)
        )
    }

    private fun getOptionValueWithRegexOrNull(args: Array<String>, regex: Regex) =
            args.findLast { it.matches(regex) }?.split("=")?.get(1)
}
