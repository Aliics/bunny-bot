package fish.eyebrow.bunnybot.handler

import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.updateUsingResource
import java.sql.Connection
import java.util.function.Consumer

class IntroHandler(private val dbConnection: Connection) : Consumer<MessageCreateEvent> {
    override fun accept(messageCreateEvent: MessageCreateEvent) {
        dbConnection.updateUsingResource("insert_intro_data.sql")
    }
}
