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
        private const val NO_MENTIONS = "Oh noes! No mention given with command!"
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
                    message.new(DiscordClientWrapper.INTERNAL_ERROR_MESSAGE)
                    logger.error("An exception occurred when handling !whois:", e)
                }
            }
        }
    }

    private fun sendResponse(result: ResultSet, message: Message) {
        val nameField = takeIfValueNonNull(result, DiscordClientWrapper.NAME_KEY)
        val ageField = takeIfValueNonNull(result, DiscordClientWrapper.AGE_KEY)
        val pronounsField = takeIfValueNonNull(result, DiscordClientWrapper.PRONOUNS_KEY)
        val extraField = takeIfValueNonNull(result, DiscordClientWrapper.EXTRA_KEY)
        message.new("$nameField\n$ageField\n$pronounsField\n$extraField")
    }

    private fun Message.new(content: String) = channel.block()?.createMessage(content)?.block()

    private fun takeIfValueNonNull(result: ResultSet, column: String): String {
        val value = result.getString(column)
        return if (value != null) "$column: $value" else ""
    }
}
