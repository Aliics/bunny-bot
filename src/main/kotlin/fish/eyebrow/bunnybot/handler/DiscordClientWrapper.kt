package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DiscordClientWrapper(private val discordClient: DiscordClient) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(DiscordClientWrapper::class.java)
    }

    fun start() {
        val eventDispatcher = discordClient.eventDispatcher
        eventDispatcher.on(MessageCreateEvent::class.java).apply {
            filter { event -> event.command("!intro") }.subscribe(IntroHandler)
        }.subscribe()
        discordClient.login().block()
    }

    private fun MessageCreateEvent.command(command: String) =
            message.content.map { content -> content.startsWith(command, ignoreCase = true) }.orElse(false)
}
