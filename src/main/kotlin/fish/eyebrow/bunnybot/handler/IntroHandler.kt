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
        private const val QUERY_WITH_DISCORD_ID = "full_query_intro_data_with_discord_id.sql"
        private const val INSERT_INTRO = "insert_intro_data.sql"
        private const val UPDATE_INTRO = "update_intro_data.sql"
        private const val ERROR_LOG_MESSAGE = "An exception occurred when updating postgres:"
        private const val ERROR_MESSAGE = "A bizarre error has occurred updating your intro :alien:"
        private const val HUMOURING_PROMPT = "Nothing to be added to your intro!"
        private const val FORMAT_OF_INTRO_HEADER = "To add stuff do it in the following format. (_only name and age are required, any order_):"
        private const val FORMAT_OF_INTRO = "name=YOUR NAME,age=YOUR AGE,pronouns=YOUR PRONOUNS,extra=YOUR EXTRA NOTES"
        private const val DISCORD_ID_MACRO = ":(discord_id)"
        private const val KEY_VALUE_DELIMITER = "="
        private val logger: Logger = LoggerFactory.getLogger(IntroHandler::class.java)
        private val commaRegex = ",".toRegex()
        private val equalsRegex = KEY_VALUE_DELIMITER.toRegex()
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        message.content.ifPresent { content ->
            try {
                val macroMap = setupMacroMap(message, content)
                if (macroMap.isNotEmpty()) {
                    upsertIntro(macroMap, message)
                } else {
                    message.new("$HUMOURING_PROMPT\n$FORMAT_OF_INTRO_HEADER\n$FORMAT_OF_INTRO")
                }
            } catch (e: Exception) {
                message.new(ERROR_MESSAGE)
                logger.error(ERROR_LOG_MESSAGE, e)
            }
        }
    }

    private fun setupMacroMap(message: Message, content: String): Map<String, String> {
        return mutableMapOf(DISCORD_ID_MACRO to message.author.get().id.asString()).let { macroMap ->
            val paramMacroMap = content.removePrefix(DiscordClientWrapper.INTRO_COMMAND)
                .trimStart()
                .split(commaRegex)
                .filter { it.contains(equalsRegex) }
                .associate {
                    val (key, value) = it.split(KEY_VALUE_DELIMITER)
                    return@associate ":($key)" to value
                }
            return@let if (paramMacroMap.isNotEmpty()) macroMap.apply { putAll(paramMacroMap) } else emptyMap()
        }
    }

    private fun upsertIntro(macroMap: Map<String, String>, message: Message) {
        val storedIntro = dbConnection.queryUsingResource(QUERY_WITH_DISCORD_ID, macroMap)
        if (!hasBeenPreviouslyStored(storedIntro)) {
            dbConnection.updateUsingResource(INSERT_INTRO, macroMap)
            message.new("Great! I've got that all setup for you, ${macroMap[":(name)"]}! :smile:")
        } else {
            dbConnection.updateUsingResource(UPDATE_INTRO, macroMap)
            message.new("Awesome! I've overwritten your previous intro, ${macroMap[":(name)"]}! :smile:")
        }
    }

    private fun Message.new(content: String) = channel.block()?.createMessage(content)?.block()

    private fun hasBeenPreviouslyStored(result: ResultSet) = result.let { it.last(); it.row > 0 }
}
