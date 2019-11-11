package fish.eyebrow.bunnybot.model

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.sql.ResultSet

internal class IntroTestCase {
    @Test
    internal fun `should initialize from map when all fields present`() {
        val expectedIntro = Intro("22", "Candi", "3", "she/her", "loves banana")
        val actualIntro = Intro.fromMap(mapOf(
            "discordId" to "22",
            "name" to "Candi",
            "age" to "3",
            "pronouns" to "she/her",
            "extra" to "loves banana"
        ))
        assertEquals(expectedIntro, actualIntro)
    }

    @Test
    internal fun `should throw when required fields are missing`() {
        assertThrows(NoSuchElementException::class.java) { Intro.fromMap(emptyMap()) }
    }

    @Test
    internal fun `should initialize from result set when all columns are present`() {
        var debounce = 0
        val resultSet = mockk<ResultSet>()
        every { resultSet.next() } answers { debounce.apply { debounce++ } < 1 }
        every { resultSet.getString("discord_id") } returns "1"
        every { resultSet.getString("name") } returns "Oliver"
        every { resultSet.getString("age") } returns "1"
        every { resultSet.getString("pronouns") } returns null
        every { resultSet.getString("extra") } returns "pwes!"
        val expectedIntros = listOf(Intro("1", "Oliver", "1", extra = "pwes!"))
        val actualIntros = Intro.fromResultSet(resultSet)
        assertEquals(expectedIntros, actualIntros)
    }

    @Test
    internal fun `should return an empty map with no results`() {
        val resultSet = mockk<ResultSet>()
        every { resultSet.next() } returns false
        val expectedIntros = emptyList<Intro>()
        val actualIntros = Intro.fromResultSet(resultSet)
        assertEquals(expectedIntros, actualIntros)
    }
}
