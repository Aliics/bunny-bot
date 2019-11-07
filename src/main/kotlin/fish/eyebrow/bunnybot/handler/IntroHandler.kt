package fish.eyebrow.bunnybot.handler

import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.updateUsingResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.sql.Connection
import java.util.function.Consumer

class IntroHandler(private val dbConnection: Connection) : Consumer<MessageCreateEvent> {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(IntroHandler::class.java)
        private val commaRegex = ",".toRegex()
        private val equalsRegex = "=".toRegex()
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        messageCreateEvent.message.apply {
            content.ifPresent { content ->
                try {
                    val macroMap = content.removePrefix(DiscordClientWrapper.INTRO_COMMAND)
                        .trimStart()
                        .split(commaRegex)
                        .filter { it.contains(equalsRegex) }
                        .associate {
                            val (key, value) = it.split("=")
                            return@associate ":($key)" to value
                        }
                    dbConnection.updateUsingResource("insert_intro_data.sql", macroMap)
                    channel.block()?.createMessage("Great! I've got that all setup for you ${macroMap[":(name)"]}! :smile:")?.block()
                } catch (e: Exception) {
                    logger.error("An exception occurred when updating postgres:", e)
                }
            }
        }
    }
}
