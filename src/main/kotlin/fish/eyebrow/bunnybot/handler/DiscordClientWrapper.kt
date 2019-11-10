package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent

class DiscordClientWrapper(private val discordClient: DiscordClient, private val introHandler: IntroHandler, private val whoIsHandler: WhoIsHandler) {
    companion object {
        internal const val INTRO_COMMAND = "!intro"
        internal const val WHOIS_COMMAND = "!whois"
        internal const val DISCORD_ID_KEY = "discordId"
        internal const val INTERNAL_ERROR_MESSAGE = "A bizarre error has occurred :alien:"
    }

    fun start() {
        val eventDispatcher = discordClient.eventDispatcher
        eventDispatcher.on(MessageCreateEvent::class.java).apply {
            filter { event -> event.command(INTRO_COMMAND) }.subscribe(introHandler)
            filter { event -> event.command(WHOIS_COMMAND) }.subscribe(whoIsHandler)
        }.subscribe()
        discordClient.login().block()
    }

    private fun MessageCreateEvent.command(command: String) =
            message.content.map { content -> content.startsWith(command, ignoreCase = true) }.orElse(false)
}
