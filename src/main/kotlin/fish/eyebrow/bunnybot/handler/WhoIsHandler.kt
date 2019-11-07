package fish.eyebrow.bunnybot.handler

import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.queryUsingResource
import java.sql.Connection
import java.sql.ResultSet
import java.util.function.Consumer

class WhoIsHandler(private val dbConnection: Connection) : Consumer<MessageCreateEvent> {
    companion object {
        private const val QUERY_INTRO_WITH_DISCORD_ID = "full_query_intro_data_with_discord_id.sql"
        private const val NO_MENTIONS = "Oh noes! No mention given with command!"
        private const val DISCORD_ID_MACRO = ":(discord_id)"
        private const val NAME_KEY = "name"
        private const val AGE_KEY = "age"
        private const val PRONOUNS_KEY = "pronouns"
        private const val EXTRA_KEY = "extra"
    }

    override fun accept(messageCreateEvent: MessageCreateEvent) {
        val message = messageCreateEvent.message
        message.userMentionIds.apply {
            ifEmpty { message.new(NO_MENTIONS) }
            forEach { snowflake ->
                val singletonDiscordIdMap = mapOf(DISCORD_ID_MACRO to snowflake.asString())
                val result = dbConnection.queryUsingResource(QUERY_INTRO_WITH_DISCORD_ID, singletonDiscordIdMap).apply { last() }
                if (result.row < 1) return@forEach
                val nameField = takeIfValueNonNull(result, NAME_KEY)
                val ageField = takeIfValueNonNull(result, AGE_KEY)
                val pronounsField = takeIfValueNonNull(result, PRONOUNS_KEY)
                val extraField = takeIfValueNonNull(result, EXTRA_KEY)
                message.new("$nameField\n$ageField\n$pronounsField\n$extraField")
            }
        }
    }

    private fun Message.new(content: String) = channel.block()?.createMessage(content)?.block()

    private fun takeIfValueNonNull(result: ResultSet, column: String) =
            if (result.getString(column) != null) "$column: ${result.getString(column)}" else ""
}
