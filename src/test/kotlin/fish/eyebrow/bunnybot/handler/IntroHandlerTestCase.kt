package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.queryUsingResource
import fish.eyebrow.bunnybot.sql.updateUsingResource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
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
    @MockK
    private lateinit var messageChannel: MessageChannel
    private lateinit var messageCreateEvent: MessageCreateEvent
    private lateinit var h2Connection: Connection
    private lateinit var introHandler: IntroHandler

    @BeforeEach
    internal fun setUp() {
        every { message.author } returns Optional.of(author)
        every { message.channel } returns Mono.just(messageChannel)
        every { messageChannel.createMessage(any<String>()) } returns Mono.just(message)
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
        val actualResultSet = getFirstRowOfQuery()
        assertEquals(expectedId, actualResultSet.getLong("id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertEquals(expectedPronouns, actualResultSet.getString("pronouns"))
        assertEquals(expectedExtra, actualResultSet.getString("extra"))
        verify { messageChannel.createMessage("Great! I've got that all setup for you Alexander! :smile:") }
    }

    @Test
    internal fun `should populate database with new intro when given brand new intro with only required fields`() {
        every { message.content } returns Optional.of("!intro name=Alfred,age=44")
        introHandler.accept(messageCreateEvent)
        val expectedId = 1L
        val expectedName = "Alfred"
        val expectedAge = "44"
        val actualResultSet = getFirstRowOfQuery()
        assertEquals(expectedId, actualResultSet.getLong("id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertNull(actualResultSet.getString("pronouns"))
        assertNull(actualResultSet.getString("extra"))
        verify { messageChannel.createMessage("Great! I've got that all setup for you Alfred! :smile:") }
    }

    @Test
    internal fun `should populate database with sanitised new intro when given brand new intro with macro-like strings`() {
        every { message.content } returns Optional.of("!intro name=Larissa,age=19,extra=hello :D")
        introHandler.accept(messageCreateEvent)
        val expectedId = 1L
        val expectedName = "Larissa"
        val expectedAge = "19"
        val expectedExtra = "hello :D"
        val actualResultSet = getFirstRowOfQuery()
        assertEquals(expectedId, actualResultSet.getLong("id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertNull(actualResultSet.getString("pronouns"))
        assertEquals(expectedExtra, actualResultSet.getString("extra"))
        verify { messageChannel.createMessage("Great! I've got that all setup for you Larissa! :smile:") }
    }

    @Test
    internal fun `should not send a message if there is a problem updating query due to a lack of fields`() {
        every { message.content } returns Optional.of("!intro name=Candi,extra=feed me >:O")
        introHandler.accept(messageCreateEvent)
        verify(exactly = 0) { messageChannel.createMessage(any<String>()) }
    }

    private fun getFirstRowOfQuery() = h2Connection.queryUsingResource("query_intro_table.sql").apply { next() }
}
