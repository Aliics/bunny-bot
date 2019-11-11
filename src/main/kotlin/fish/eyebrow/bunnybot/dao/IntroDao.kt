package fish.eyebrow.bunnybot.dao

import fish.eyebrow.bunnybot.model.Intro
import fish.eyebrow.bunnybot.model.IntroFactory
import fish.eyebrow.bunnybot.util.collectFilePathData
import java.sql.Connection
import java.sql.ResultSet

class IntroDao(private val dbConnection: Connection) {
    companion object {
        private const val INSERT_INTRO_DATA_SQL = "insert_intro_data.sql"
        private const val UPDATE_INTRO_DATA_SQL = "update_intro_data.sql"
        private const val QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL = "query_intro_data_with_discord_id.sql"

    }

    fun insertIntro(intro: Intro) {
        prepareScrollableStatement(INSERT_INTRO_DATA_SQL).apply {
            setString(1, intro.discordId)
            setString(2, intro.name)
            setString(3, intro.age)
            setString(4, intro.pronouns)
            setString(5, intro.extra)
            setString(6, intro.icon)
            executeUpdate()
        }
    }

    fun updateIntro(intro: Intro) {
        prepareScrollableStatement(UPDATE_INTRO_DATA_SQL).apply {
            setString(1, intro.name)
            setString(2, intro.age)
            setString(3, intro.pronouns)
            setString(4, intro.extra)
            setString(5, intro.icon)
            setString(6, intro.discordId)
            executeUpdate()
        }
    }

    fun findIntroWithDiscordId(discordId: String?): List<Intro> {
        val internalIntroResultSet = prepareScrollableStatement(QUERY_INTRO_DATA_WITH_DISCORD_ID_SQL).run {
            setString(1, discordId)
            return@run executeQuery()
        }
        return IntroFactory.fromResultSet(internalIntroResultSet)
    }

    private fun prepareScrollableStatement(sqlResource: String) =
            dbConnection.prepareStatement(collectFilePathData(sqlResource), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)
}
