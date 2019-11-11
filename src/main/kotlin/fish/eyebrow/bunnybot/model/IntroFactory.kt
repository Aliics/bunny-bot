package fish.eyebrow.bunnybot.model

import java.sql.ResultSet

object IntroFactory {
    private const val DISCORD_INTERNAL_KEY = "discordId"
    private const val DISCORD_EXTERNAL_KEY = "discord_id"
    private const val NAME_KEY = "name"
    private const val AGE_KEY = "age"
    private const val PRONOUNS_KEY = "pronouns"
    private const val EXTRA_KEY = "extra"
    private const val ICON_KEY = "icon"

    fun fromMap(map: Map<String, String>) = Intro(
        discordId = map.getValue(DISCORD_INTERNAL_KEY),
        name = map.getValue(NAME_KEY),
        age = map.getValue(AGE_KEY),
        pronouns = map[PRONOUNS_KEY],
        extra = map[EXTRA_KEY],
        icon = map[ICON_KEY]
    )

    fun fromResultSet(resultSet: ResultSet) = mutableListOf<Intro>().apply {
        while (resultSet.next()) {
            add(Intro(
                discordId = resultSet.getString(DISCORD_EXTERNAL_KEY),
                name = resultSet.getString(NAME_KEY),
                age = resultSet.getString(AGE_KEY),
                pronouns = resultSet.getString(PRONOUNS_KEY),
                extra = resultSet.getString(EXTRA_KEY),
                icon = resultSet.getString(ICON_KEY)
            ))
        }
    }
}
