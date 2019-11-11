package fish.eyebrow.bunnybot.handler

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.dao.IntroDao
import fish.eyebrow.bunnybot.model.Intro
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
                    val mention = message.guild.block()?.getMemberById(snowflake)?.block()?.mention
                    val intros = introDao.findIntroWithDiscordId(discordId)
                    if (intros.isEmpty()) {
                        message.new("Uh oh! $mention has no intro yet!")
                        return@forEach
                    }
                    sendResponse(intros.first(), message)
                } catch (e: Exception) {
                    message.new(DiscordClientWrapper.INTERNAL_ERROR_MESSAGE)
                    logger.error("An exception occurred when handling !whois:", e)
                }
            }
        }
    }

    private fun sendResponse(intro: Intro, message: Message) {
        val iconPrefix = if (intro.icon != null) "${intro.icon} " else ""
        val nameField = "${iconPrefix}name: ${intro.name}"
        val ageField = "${iconPrefix}age: ${intro.age}"
        val pronounsField = "${iconPrefix}pronouns: ${intro.pronouns}"
        val extraField = "${iconPrefix}extra: ${intro.extra}"
        message.new("$nameField\n$ageField\n$pronounsField\n$extraField")
    }

    private fun Message.new(content: String) = channel.block()?.createMessage(content)?.block()
}
