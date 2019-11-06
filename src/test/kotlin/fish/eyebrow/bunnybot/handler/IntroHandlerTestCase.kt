package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.queryUsingResource
import fish.eyebrow.bunnybot.sql.updateUsingResource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

@ExtendWith(MockKExtension::class)
internal class IntroHandlerTestCase {
    @MockK
    private lateinit var discordClient: DiscordClient
    @MockK
    private lateinit var message: Message
    @MockK
    private lateinit var author: User
    private lateinit var messageCreateEvent: MessageCreateEvent
    private lateinit var h2Connection: Connection
    private lateinit var introHandler: IntroHandler

    @BeforeEach
    internal fun setUp() {
        every { message.author } returns Optional.of(author)
        messageCreateEvent = MessageCreateEvent(discordClient, message, null, null)
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        introHandler = IntroHandler(h2Connection)
        h2Connection.updateUsingResource("create_intro_table.sql")
    }

    @AfterEach
    internal fun tearDown() {
        h2Connection.updateUsingResource("delete_intro_table.sql")
    }

    @Test
    internal fun `should populate database with new intro when given brand new intro with all fields`() {
        every { message.content } returns Optional.of("!intro name=Alexander,age=20,pronouns=he/him,extra=likes pepsi and rabbits")
        introHandler.accept(messageCreateEvent)
        val expectedId = 1L
        val expectedName = "Alexander"
        val expectedAge = "20"
        val expectedPronouns = "he/him"
        val expectedExtra = "likes pepsi and rabbits"
        val actualResultSet = h2Connection.queryUsingResource("query_intro_table.sql").apply { next() }
        assertEquals(expectedId, actualResultSet.getLong("id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertEquals(expectedPronouns, actualResultSet.getString("pronouns"))
        assertEquals(expectedExtra, actualResultSet.getString("extra"))
    }
}
