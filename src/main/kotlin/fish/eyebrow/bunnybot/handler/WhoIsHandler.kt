package fish.eyebrow.bunnybot.handler

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.dao.IntroDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.ResultSet
import java.util.function.Consumer

class WhoIsHandler(private val introDao: IntroDao) : Consumer<MessageCreateEvent> {
    companion object {
        private const val ERROR_LOG_MESSAGE = "An exception occurred when querying postgres:"
        private const val NO_MENTIONS = "Oh noes! No mention given with command!"
        private const val ERROR_MESSAGE = "A bizarre error has occurred updating your intro :alien:"
        private const val NAME_KEY = "name"
        private const val AGE_KEY = "age"
        private const val PRONOUNS_KEY = "pronouns"
        private const val EXTRA_KEY = "extra"
        private val logger: Logger = LoggerFactory.getLogger(WhoIsHandler::class.java)
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        message.userMentionIds.apply {
            ifEmpty { message.new(NO_MENTIONS) }
            forEach { snowflake ->
                try {
                    val discordId = snowflake.asString()
                    val result = introDao.findIntroWithDiscordId(discordId)
                    if (result.row < 1) return@forEach
                    sendResponse(result, message)
                } catch (e: Exception) {
                    message.new(ERROR_MESSAGE)
                    logger.error(ERROR_LOG_MESSAGE, e)
                }
            }
        }
    }

    private fun sendResponse(result: ResultSet, message: Message) {
        val nameField = takeIfValueNonNull(result, NAME_KEY)
        val ageField = takeIfValueNonNull(result, AGE_KEY)
        val pronounsField = takeIfValueNonNull(result, PRONOUNS_KEY)
        val extraField = takeIfValueNonNull(result, EXTRA_KEY)
        message.new("$nameField\n$ageField\n$pronounsField\n$extraField")
    }

    private fun Message.new(content: String) = channel.block()?.createMessage(content)?.block()

    private fun takeIfValueNonNull(result: ResultSet, column: String): String {
        val value = result.getString(column)
        return if (value != null) "$column: $value" else ""
    }
}
