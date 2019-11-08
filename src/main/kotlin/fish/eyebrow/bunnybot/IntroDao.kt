package fish.eyebrow.bunnybot

import fish.eyebrow.bunnybot.util.collectFilePathData
import java.sql.Connection
import java.sql.ResultSet

class IntroDao(private val dbConnection: Connection) {
    fun insertIntro(introMap: Map<String, String>) {
        dbConnection.prepareStatement(collectFilePathData("insert_intro_data.sql")).apply {
            setString(1, introMap["discordId"])
            setString(2, introMap["name"])
            setString(3, introMap["age"])
            setString(4, introMap["pronouns"])
            setString(5, introMap["extra"])
            executeUpdate()
        }
    }

    fun updateIntro(introMap: Map<String, String>) {
        dbConnection.prepareStatement(collectFilePathData("update_intro_data.sql")).apply {
            setString(1, introMap["name"])
            setString(2, introMap["age"])
            setString(3, introMap["pronouns"])
            setString(4, introMap["extra"])
            setString(5, introMap["discordId"])
            executeUpdate()
        }
    }

    fun findIntroWithDiscordId(discordId: String?): ResultSet {
        val preparedStatement = dbConnection.prepareStatement(collectFilePathData("query_intro_data_with_discord_id.sql"))
        preparedStatement.setString(1, discordId)
        return preparedStatement.executeQuery()
    }
}
