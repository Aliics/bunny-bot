package fish.eyebrow.bunnybot.handler

import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.updateUsingResource
import java.sql.Connection
import java.util.function.Consumer

class IntroHandler(private val dbConnection: Connection) : Consumer<MessageCreateEvent> {
    companion object {
        private val commaRegex = ",".toRegex()
        private val equalsRegex = "=".toRegex()
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        messageCreateEvent.message.content.ifPresent { content ->
            val macroMap = content.removePrefix(DiscordClientWrapper.INTRO_COMMAND)
                .trimStart()
                .split(commaRegex)
                .filter { it.contains(equalsRegex) }
                .associate {
                    val (key, value) = it.split("=")
                    return@associate ":($key)" to value
                }
            dbConnection.updateUsingResource("insert_intro_data.sql", macroMap)
        }
    }
}
