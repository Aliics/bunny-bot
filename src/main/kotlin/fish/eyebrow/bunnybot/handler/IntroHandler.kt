package fish.eyebrow.bunnybot.handler

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.dao.IntroDao
import fish.eyebrow.bunnybot.model.Intro
import fish.eyebrow.bunnybot.model.IntroFactory
import fish.eyebrow.bunnybot.role.RoleAssigner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Consumer

class IntroHandler(private val introDao: IntroDao) : Consumer<MessageCreateEvent> {
    companion object {
        private const val HUMOURING_PROMPT = "Nothing to be added to your intro!"
        private const val FORMAT_OF_INTRO_HEADER = "To add stuff do it in the following format. (_only name and age are required, any order_):"
        private const val FORMAT_OF_INTRO = "name=YOUR NAME,age=YOUR AGE,pronouns=YOUR PRONOUNS,extra=YOUR EXTRA NOTES"
        private const val KEY_VALUE_DELIMITER = "="
        private val logger: Logger = LoggerFactory.getLogger(IntroHandler::class.java)
        private val commaRegex = ",".toRegex()
        private val equalsRegex = KEY_VALUE_DELIMITER.toRegex()
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        message.content.ifPresent { content ->
            try {
                val discordId = message.author.get().id.asString()
                val intro = collectIntroFromMessageContent(discordId, content)
                upsertIntroWithMessage(intro, message)
                RoleAssigner.assignAge(intro.age, message)
            } catch (e: NoSuchElementException) {
                message.new("$HUMOURING_PROMPT\n$FORMAT_OF_INTRO_HEADER\n$FORMAT_OF_INTRO")
            } catch (e: Exception) {
                message.new(DiscordClientWrapper.INTERNAL_ERROR_MESSAGE)
                logger.error("An exception occurred when handling !intro:", e)
            }
        }
    }

    private fun collectIntroFromMessageContent(discordId: String, content: String): Intro {
        val internalIntroMap = mutableMapOf(DiscordClientWrapper.DISCORD_ID_KEY to discordId).let { introMap ->
            val introParamMap = content.removePrefix(DiscordClientWrapper.INTRO_COMMAND)
                .trimStart()
                .split(commaRegex)
                .filter { it.contains(equalsRegex) }
                .associate {
                    val (key, value) = it.split(KEY_VALUE_DELIMITER)
                    return@associate key to value
                }
            return@let if (introParamMap.isNotEmpty()) introMap.apply { putAll(introParamMap) } else emptyMap<String, String>()
        }
        return IntroFactory.fromMap(internalIntroMap)
    }

    private fun upsertIntroWithMessage(intro: Intro, message: Message) {
        val storedIntro = introDao.findIntroWithDiscordId(intro.discordId)
        if (storedIntro.isEmpty()) {
            introDao.insertIntro(intro)
            message.new("Great! I've got that all setup for you, ${intro.name}! :smile:")
        } else {
            introDao.updateIntro(intro)
            message.new("Awesome! I've overwritten your previous intro, ${intro.name}! :smile:")
        }
    }

    private fun Message.new(content: String) = channel.block()?.createMessage(content)?.block()
}
