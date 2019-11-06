package fish.eyebrow.bunnybot

import discord4j.core.DiscordClientBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object App {
    private val logger: Logger = LoggerFactory.getLogger(App::class.java)

    @JvmStatic
    fun main(arg: Array<String>) {
        try {
            val commandLineArguments = CommandLineParser.parse(arg)
            DiscordClientBuilder(commandLineArguments.token!!)
        } catch (e: Exception) {
            logger.error("Exception occurred when setting up bot: {}", e.message)
        }
    }
}
