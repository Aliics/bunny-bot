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
                if (macroMap.isNotEmpty()) {
                    upsertIntro(macroMap, message)
                } else {
                    val humouringPrompt = "Nothing to be added to your intro!"
                    val listOfIntroHeader = "To add stuff do it in the following format. (_only name and age are required, any order_):"
                    val listOfIntro = "name=YOUR NAME,age=YOUR AGE,pronouns=YOUR PRONOUNS,extra=YOUR EXTRA NOTES"
                    message.new("$humouringPrompt\n$listOfIntroHeader\n$listOfIntro")
                }
            } catch (e: Exception) {
                message.new("A bizarre error has occurred updating your intro :alien:")
                logger.error("An exception occurred when updating postgres:", e)
            }
        }
    }

    private fun setupMacroMap(message: Message, content: String): Map<String, String> {
        return mutableMapOf(":(discord_id)" to message.author.get().id.asString()).let { macroMap ->
            val paramMacroMap = content.removePrefix(DiscordClientWrapper.INTRO_COMMAND)
                .trimStart()
                .split(commaRegex)
                .filter { it.contains(equalsRegex) }
                .associate {
                    val (key, value) = it.split("=")
                    return@associate ":($key)" to value
                }
            return@let if (paramMacroMap.isNotEmpty()) macroMap.apply { putAll(paramMacroMap) } else emptyMap()
        }
    }

    private fun upsertIntro(macroMap: Map<String, String>, message: Message) {
        val storedIntro = dbConnection.queryUsingResource("full_query_intro_data_with_discord_id.sql", macroMap)
        if (!hasBeenPreviouslyStored(storedIntro)) {
            dbConnection.updateUsingResource("insert_intro_data.sql", macroMap)
            message.new("Great! I've got that all setup for you, ${macroMap[":(name)"]}! :smile:")
        } else {
            dbConnection.updateUsingResource("update_intro_data.sql", macroMap)
            message.new("Awesome! I've overwritten your previous intro, ${macroMap[":(name)"]}! :smile:")
        }
    }

    private fun Message.new(content: String) {
        channel.block()?.createMessage(content)?.block()
    }

    private fun hasBeenPreviouslyStored(result: ResultSet) = result.let { it.last(); it.row > 0 }
}
