package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.event.domain.message.MessageCreateEvent
import java.util.function.Consumer

class EchoHandler(private val discordClient: DiscordClient) : Consumer<MessageCreateEvent> {
    override fun accept(messageCreateEvent: MessageCreateEvent) {

    }
}