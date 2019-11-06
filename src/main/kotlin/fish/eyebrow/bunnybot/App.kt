package fish.eyebrow.bunnybot

import com.google.inject.Guice
import fish.eyebrow.bunnybot.handler.DiscordClientWrapper
import fish.eyebrow.bunnybot.module.BunnyBotModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object App {
    private val logger: Logger = LoggerFactory.getLogger(App::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val injector = Guice.createInjector(BunnyBotModule(CommandLineParser.parse(args)))
            val discordClientWrapper = injector.getInstance(DiscordClientWrapper::class.java)
            discordClientWrapper.start()
        } catch (e: Exception) {
            logger.error("Exception occurred when setting up bot: {}", e.message)
        }
    }
}
