package fish.eyebrow.bunnybot.dao

import fish.eyebrow.bunnybot.handler.DiscordClientWrapper
import fish.eyebrow.bunnybot.util.collectFilePathData
import java.sql.Connection
import java.sql.ResultSet

class IntroDao(private val dbConnection: Connection) {
    companion object {
        private const val INSERT_INTRO_DATA_SQL = "insert_intro_data.sql"
        private const val UPDATE_INTRO_DATA_SQL = "update_intro_data.sql"
        private const val QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL = "query_intro_data_with_discord_id.sql"

    }

    fun insertIntro(introMap: Map<String, String>) {
        prepareScrollableStatement(INSERT_INTRO_DATA_SQL).apply {
            setString(1, introMap[DiscordClientWrapper.DISCORD_ID_KEY])
            setString(2, introMap[DiscordClientWrapper.NAME_KEY])
            setString(3, introMap[DiscordClientWrapper.AGE_KEY])
            setString(4, introMap[DiscordClientWrapper.PRONOUNS_KEY])
            setString(5, introMap[DiscordClientWrapper.EXTRA_KEY])
            executeUpdate()
        }
    }

    fun updateIntro(introMap: Map<String, String>) {
        prepareScrollableStatement(UPDATE_INTRO_DATA_SQL).apply {
            setString(1, introMap[DiscordClientWrapper.NAME_KEY])
            setString(2, introMap[DiscordClientWrapper.AGE_KEY])
            setString(3, introMap[DiscordClientWrapper.PRONOUNS_KEY])
            setString(4, introMap[DiscordClientWrapper.EXTRA_KEY])
            setString(5, introMap[DiscordClientWrapper.DISCORD_ID_KEY])
            executeUpdate()
        }
    }

    fun findIntroWithDiscordId(discordId: String?): ResultSet {
        return prepareScrollableStatement(QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL).run {
            setString(1, discordId)
            return@run executeQuery().apply { last() }
        }
    }

    private fun prepareScrollableStatement(sqlResource: String) =
            dbConnection.prepareStatement(collectFilePathData(sqlResource), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
}
