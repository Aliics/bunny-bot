package fish.eyebrow.bunnybot.model

data class Intro(
        val discordId: String,
        val name: String,
        val age: String,
        val pronouns: String? = null,
        val extra: String? = null,
        val icon: String? = null
)
