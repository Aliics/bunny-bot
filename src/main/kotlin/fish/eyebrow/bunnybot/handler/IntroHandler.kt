package fish.eyebrow.bunnybot.handler

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.dao.IntroDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Consumer

class IntroHandler(private val introDao: IntroDao) : Consumer<MessageCreateEvent> {
    companion object {
        private const val ERROR_LOG_MESSAGE = "An exception occurred when updating postgres:"
        private const val ERROR_MESSAGE = "A bizarre error has occurred updating your intro :alien:"
        private const val HUMOURING_PROMPT = "Nothing to be added to your intro!"
        private const val FORMAT_OF_INTRO_HEADER = "To add stuff do it in the following format. (_only name and age are required, any order_):"
        private const val FORMAT_OF_INTRO = "name=YOUR NAME,age=YOUR AGE,pronouns=YOUR PRONOUNS,extra=YOUR EXTRA NOTES"
        private const val DISCORD_ID_KEY = "discordId"
        private const val NAME_KEY = "name"
        private const val KEY_VALUE_DELIMITER = "="
        private val logger: Logger = LoggerFactory.getLogger(IntroHandler::class.java)
        private val commaRegex = ",".toRegex()
        private val equalsRegex = KEY_VALUE_DELIMITER.toRegex()
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        message.content.ifPresent { content ->
            try {
                val introMap = setupIntroFieldMap(message, content)
                if (introMap.isNotEmpty()) {
                    upsertIntroWithMessage(introMap, message)
                } else {
                    message.new("$HUMOURING_PROMPT\n$FORMAT_OF_INTRO_HEADER\n$FORMAT_OF_INTRO")
                }
            } catch (e: Exception) {
                message.new(ERROR_MESSAGE)
                logger.error(ERROR_LOG_MESSAGE, e)
            }
        }
    }

    private fun setupIntroFieldMap(message: Message, content: String): Map<String, String> {
        return mutableMapOf(DISCORD_ID_KEY to message.author.get().id.asString()).let { introMap ->
            val introParamMap = content.removePrefix(DiscordClientWrapper.INTRO_COMMAND)
                .trimStart()
                .split(commaRegex)
                .filter { it.contains(equalsRegex) }
                .associate {
                    val (key, value) = it.split(KEY_VALUE_DELIMITER)
                    return@associate key to value
                }
            return@let if (introParamMap.isNotEmpty()) introMap.apply { putAll(introParamMap) } else emptyMap()
        }
    }

    private fun upsertIntroWithMessage(introMap: Map<String, String>, message: Message) {
        val storedIntro = introDao.findIntroWithDiscordId(introMap[DISCORD_ID_KEY])
        val introName = introMap[NAME_KEY]
        if (storedIntro.row < 1) {
            introDao.insertIntro(introMap)
            message.new("Great! I've got that all setup for you, $introName! :smile:")
        } else {
            introDao.updateIntro(introMap)
            message.new("Awesome! I've overwritten your previous intro, $introName! :smile:")
        }
    }

    private fun Message.new(content: String) = channel.block()?.createMessage(content)?.block()
}
