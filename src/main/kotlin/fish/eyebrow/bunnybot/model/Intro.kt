package fish.eyebrow.bunnybot.model

import java.sql.ResultSet

data class Intro(val discordId: String, val name: String, val age: String, val pronouns: String? = null, val extra: String? = null) {
    companion object {
        fun fromMap(map: Map<String, String>) = Intro(
            discordId = map.getValue("discordId"),
            name = map.getValue("name"),
            age = map.getValue("age"),
            pronouns = map["pronouns"],
            extra = map["extra"]
        )

        fun fromResultSet(resultSet: ResultSet) = mutableListOf<Intro>().apply {
            while (resultSet.next()) {
                add(Intro(
                    discordId = resultSet.getString("discord_id"),
                    name = resultSet.getString("name"),
                    age = resultSet.getString("age"),
                    pronouns = resultSet.getString("pronouns"),
                    extra = resultSet.getString("extra")
                ))
            }
        }
    }
}
