package fish.eyebrow.bunnybot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CommandLineArgumentsTestCase {
    @Test
    internal fun `should populate token when passed with option`() {
        val args = arrayOf("-token=ead74f3c-827f-4ce8-ac3b-929d63fb75aa")
        val expectedToken = "ead74f3c-827f-4ce8-ac3b-929d63fb75aa"
        val actualToken = CommandLineParser.parse(args).token
        assertEquals(expectedToken, actualToken)
    }

    @Test
    fun `should populate dbUrl when passed with option`() {
        val args = arrayOf("-db.url=jdbc:psql://0.0.0.0:5432/")
        val expectedDbUrl = "jdbc:psql://0.0.0.0:5432/"
        val actualDbUrl = CommandLineParser.parse(args).dbUrl
        assertEquals(expectedDbUrl, actualDbUrl)
    }

    @Test
    fun `should populate dbUser when passed with option`() {
        val args = arrayOf("-db.user=root")
        val expectedDbUser = "root"
        val actualDbUser = CommandLineParser.parse(args).dbUser
        assertEquals(expectedDbUser, actualDbUser)
    }

    @Test
    fun `should populate dbPassword when passed with option`() {
        val args = arrayOf("-db.password=password")
        val expectedDbPassword = "password"
        val actualDbPassword = CommandLineParser.parse(args).dbPassword
        assertEquals(expectedDbPassword, actualDbPassword)
    }
}
