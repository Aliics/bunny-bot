package fish.eyebrow.bunnybot.handler

import discord4j.core.`object`.entity.Message

object RoleAssigner {
    private const val ADULT_ROLE_NAME = "bunnies"
    private const val MINOR_ROLE_NAME = "lil bunnies"

    fun assignAge(age: String, message: Message) {
        message.guild.block()?.roles?.subscribe { role ->
            val ageGroup = parseAgeGroup(age)
            val shouldAssign = when (role.name) {
                ADULT_ROLE_NAME -> ageGroup == AgeGroup.ADULT
                MINOR_ROLE_NAME -> ageGroup == AgeGroup.MINOR
                else -> false
            }
            if (shouldAssign) message.authorAsMember.block()?.addRole(role.id)
        }
    }

    private fun parseAgeGroup(age: String) = try {
        if (age.toInt() >= 18) AgeGroup.ADULT else AgeGroup.MINOR
    } catch (e: Exception) {
        AgeGroup.values().find { ageGroup -> ageGroup.name == age.toUpperCase() } ?: AgeGroup.UNKNOWN
    }
}

enum class AgeGroup {
    ADULT,
    MINOR,
    UNKNOWN
}
