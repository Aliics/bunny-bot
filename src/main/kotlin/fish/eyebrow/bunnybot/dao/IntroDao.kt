package fish.eyebrow.bunnybot.dao

import fish.eyebrow.bunnybot.util.collectFilePathData
import java.sql.Connection
import java.sql.ResultSet

class IntroDao(private val dbConnection: Connection) {
    companion object {
        private const val INSERT_INTRO_DATA_SQL = "insert_intro_data.sql"
        private const val UPDATE_INTRO_DATA_SQL = "update_intro_data.sql"
        private const val QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL = "query_intro_data_with_discord_id.sql"
        private const val DISCORD_ID_KEY = "discordId"
        private const val NAME_KEY = "name"
        private const val AGE_KEY = "age"
        private const val PRONOUNS_KEY = "pronouns"
        private const val EXTRA_KEY = "extra"
    }

    fun insertIntro(introMap: Map<String, String>) {
        dbConnection.prepareStatement(collectFilePathData(INSERT_INTRO_DATA_SQL)).apply {
            setString(1, introMap[DISCORD_ID_KEY])
            setString(2, introMap[NAME_KEY])
            setString(3, introMap[AGE_KEY])
            setString(4, introMap[PRONOUNS_KEY])
            setString(5, introMap[EXTRA_KEY])
            executeUpdate()
        }
    }

    fun updateIntro(introMap: Map<String, String>) {
        dbConnection.prepareStatement(collectFilePathData(UPDATE_INTRO_DATA_SQL)).apply {
            setString(1, introMap[NAME_KEY])
            setString(2, introMap[AGE_KEY])
            setString(3, introMap[PRONOUNS_KEY])
            setString(4, introMap[EXTRA_KEY])
            setString(5, introMap[DISCORD_ID_KEY])
            executeUpdate()
        }
    }

    fun findIntroWithDiscordId(discordId: String?): ResultSet {
        return dbConnection.prepareStatement(collectFilePathData(QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL)).run {
            setString(1, discordId)
            return@run executeQuery().apply { last() }
        }
    }
}
