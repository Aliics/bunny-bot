package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent

class DiscordClientWrapper(
        private val discordClient: DiscordClient,
        private val echoHandler: EchoHandler
) {
    fun start() {
        val eventDispatcher = discordClient.eventDispatcher
        eventDispatcher.on(MessageCreateEvent::class.java).subscribe(echoHandler)
        discordClient.login().block()
    }
}
