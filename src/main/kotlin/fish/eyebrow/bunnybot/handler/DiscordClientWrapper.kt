package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent

class DiscordClientWrapper(private val discordClient: DiscordClient, private val introHandler: IntroHandler) {
    companion object {
        private const val INTRO_COMMAND = "!intro"
    }

    fun start() {
        val eventDispatcher = discordClient.eventDispatcher
        eventDispatcher.on(MessageCreateEvent::class.java).apply {
            filter { event -> event.command(INTRO_COMMAND) }.subscribe(introHandler)
        }.subscribe()
        discordClient.login().block()
    }

    private fun MessageCreateEvent.command(command: String) =
            message.content.map { content -> content.startsWith(command, ignoreCase = true) }.orElse(false)
}
