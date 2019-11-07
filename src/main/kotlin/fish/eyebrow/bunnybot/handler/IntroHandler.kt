package fish.eyebrow.bunnybot.handler

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.queryUsingResource
import fish.eyebrow.bunnybot.sql.updateUsingResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.ResultSet
import java.util.function.Consumer

class IntroHandler(private val dbConnection: Connection) : Consumer<MessageCreateEvent> {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(IntroHandler::class.java)
        private val commaRegex = ",".toRegex()
        private val equalsRegex = "=".toRegex()
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        message.content.ifPresent { content ->
            try {
                val macroMap = setupMacroMap(message, content)
                upsertIntro(macroMap, message)
            } catch (e: Exception) {
                logger.error("An exception occurred when updating postgres:", e)
            }
        }
    }

    private fun setupMacroMap(message: Message, content: String): MutableMap<String, String> {
        return mutableMapOf(":(discord_id)" to message.author.get().id.asString()).apply {
            val paramMacroMap = content.removePrefix(DiscordClientWrapper.INTRO_COMMAND)
                .trimStart()
                .split(commaRegex)
                .filter { it.contains(equalsRegex) }
                .associate {
                    val (key, value) = it.split("=")
                    return@associate ":($key)" to value
                }
            putAll(paramMacroMap)
        }
    }

    private fun upsertIntro(rootMacroMap: MutableMap<String, String>, message: Message) {
        val storedIntro = dbConnection.queryUsingResource("full_query_intro_data_with_discord_id.sql", rootMacroMap)
        if (!hasBeenPreviouslyStored(storedIntro)) {
            dbConnection.updateUsingResource("insert_intro_data.sql", rootMacroMap)
            message.channel.block()?.createMessage("Great! I've got that all setup for you, ${rootMacroMap[":(name)"]}! :smile:")?.block()
        } else {
            dbConnection.updateUsingResource("update_intro_data.sql", rootMacroMap)
            message.channel.block()?.createMessage("Awesome! I've overwritten your previous intro, ${rootMacroMap[":(name)"]}! :smile:")?.block()
        }
    }

    private fun hasBeenPreviouslyStored(result: ResultSet) = result.let { it.last(); it.row > 0 }
}
