package fish.eyebrow.bunnybot.handler

import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.queryUsingResource
import java.sql.Connection
import java.sql.ResultSet
import java.util.function.Consumer

class WhoIsHandler(private val dbConnection: Connection) : Consumer<MessageCreateEvent> {
    override fun accept(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        message.userMentionIds.forEach { snowflake ->
            val singletonDiscordIdMap = mapOf(":(discord_id)" to snowflake.asString())
            val result = dbConnection.queryUsingResource("full_query_intro_data_with_discord_id.sql", singletonDiscordIdMap).apply { next() }
            val nameField = takeIfValueNonNull(result, "name")
            val ageField = takeIfValueNonNull(result, "age")
            val pronounsField = takeIfValueNonNull(result, "pronouns")
            val extraField = takeIfValueNonNull(result, "extra")
            message.channel.block()?.createMessage("$nameField\n$ageField\n$pronounsField\n$extraField")?.block()
        }
    }

    private fun takeIfValueNonNull(result: ResultSet, column: String) =
            if (result.getString(column) != null) "$column: ${result.getString(column)}" else ""
}
