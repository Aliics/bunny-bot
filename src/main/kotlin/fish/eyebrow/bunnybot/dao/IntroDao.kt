package fish.eyebrow.bunnybot.dao

import fish.eyebrow.bunnybot.model.Intro
import fish.eyebrow.bunnybot.util.collectFilePathData
import java.sql.Connection
import java.sql.ResultSet

class IntroDao(private val dbConnection: Connection) {
    companion object {
        private const val INSERT_INTRO_DATA_SQL = "insert_intro_data.sql"
        private const val UPDATE_INTRO_DATA_SQL = "update_intro_data.sql"
        private const val QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL = "query_intro_data_with_discord_id.sql"

    }

    fun insertIntro(introMap: Intro) {
        prepareScrollableStatement(INSERT_INTRO_DATA_SQL).apply {
            setString(1, introMap.discordId)
            setString(2, introMap.name)
            setString(3, introMap.age)
            setString(4, introMap.pronouns)
            setString(5, introMap.extra)
            executeUpdate()
        }
    }

    fun updateIntro(introMap: Intro) {
        prepareScrollableStatement(UPDATE_INTRO_DATA_SQL).apply {
            setString(1, introMap.name)
            setString(2, introMap.age)
            setString(3, introMap.pronouns)
            setString(4, introMap.extra)
            setString(5, introMap.discordId)
            executeUpdate()
        }
    }

    fun findIntroWithDiscordId(discordId: String?): List<Intro> {
        return Intro.fromResultSet(prepareScrollableStatement(QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL).run {
            setString(1, discordId)
            return@run executeQuery()
        })
    }

    private fun prepareScrollableStatement(sqlResource: String) =
            dbConnection.prepareStatement(collectFilePathData(sqlResource), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
}
