package fish.eyebrow.bunnybot.handler

import discord4j.core.event.domain.message.MessageCreateEvent
import java.util.function.Consumer

object IntroHandler : Consumer<MessageCreateEvent> {
    override fun accept(messageCreateEvent: MessageCreateEvent) {
    }
}
