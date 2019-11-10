package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.dao.IntroDao
import fish.eyebrow.bunnybot.util.collectFilePathData
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
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
        introHandler = IntroHandler(IntroDao(h2Connection))
        h2Connection.createStatement().executeUpdate(collectFilePathData("create_intro_table.sql"))
    }

    @AfterEach
    internal fun tearDown() {
        h2Connection.createStatement().executeUpdate(collectFilePathData("delete_intro_table.sql"))
    }

    @Test
    internal fun `should populate database with new intro when given brand new intro with all fields`() {
        val expectedDiscordId = "48293"
        val expectedName = "Alexander"
        val expectedAge = "20"
        val expectedPronouns = "he/him"
        val expectedExtra = "likes pepsi and rabbits"
        whenHandlerIsInvokedAsAuthorWithMessage(
            authorId = expectedDiscordId,
            messageContent = "!intro name=${expectedName},age=${expectedAge},pronouns=${expectedPronouns},extra=${expectedExtra}"
        )
        val actualResultSet = getFirstRowOfQuery()
        assertEquals(expectedDiscordId, actualResultSet.getString("discord_id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertEquals(expectedPronouns, actualResultSet.getString("pronouns"))
        assertEquals(expectedExtra, actualResultSet.getString("extra"))
        verify { messageChannel.createMessage("Great! I've got that all setup for you, Alexander! :smile:") }
    }

    @Test
    internal fun `should populate database with new intro when given brand new intro with only required fields`() {
        val expectedDiscordId = "88820"
        val expectedName = "Alfred"
        val expectedAge = "44"
        whenHandlerIsInvokedAsAuthorWithMessage(expectedDiscordId, "!intro name=${expectedName},age=${expectedAge}")
        val actualResultSet = getFirstRowOfQuery()
        assertEquals(expectedDiscordId, actualResultSet.getString("discord_id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertNull(actualResultSet.getString("pronouns"))
        assertNull(actualResultSet.getString("extra"))
        verify { messageChannel.createMessage("Great! I've got that all setup for you, Alfred! :smile:") }
    }

    @Test
    internal fun `should populate database with sanitised new intro when given brand new intro with macro-like strings`() {
        val expectedDiscordId = "7777777"
        val expectedName = "Larissa"
        val expectedAge = "19"
        val expectedExtra = "hello :D"
        whenHandlerIsInvokedAsAuthorWithMessage(expectedDiscordId, "!intro name=${expectedName},age=${expectedAge},extra=${expectedExtra}")
        val actualResultSet = getFirstRowOfQuery()
        assertEquals(expectedDiscordId, actualResultSet.getString("discord_id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertNull(actualResultSet.getString("pronouns"))
        assertEquals(expectedExtra, actualResultSet.getString("extra"))
        verify { messageChannel.createMessage("Great! I've got that all setup for you, Larissa! :smile:") }
    }

    @Test
    internal fun `should respond with an error message when no discord id is attached`() {
        every { message.content } returns Optional.of("!intro name=Candi,extra=feed me >:O")
        introHandler.accept(messageCreateEvent)
        verify { messageChannel.createMessage("A bizarre error has occurred :alien:") }
    }

    @Test
    internal fun `should overwrite data in database when there is already an existing row`() {
        val expectedDiscordId = "111"
        val expectedName = "Oliver"
        val expectedAge = "1"
        val expectedExtra = "pwes! big wuv! :carrot: :heart:"
        whenHandlerIsInvokedAsAuthorWithMessage(expectedDiscordId, "!intro name=${expectedName},age=${expectedAge},extra=pwes! :carrot:")
        whenHandlerIsInvokedAsAuthorWithMessage(expectedDiscordId, "!intro name=${expectedName},age=${expectedAge},extra=${expectedExtra}")
        val actualResultSet = getFirstRowOfQuery()
        assertEquals(expectedDiscordId, actualResultSet.getString("discord_id"))
        assertEquals(expectedName, actualResultSet.getString("name"))
        assertEquals(expectedAge, actualResultSet.getString("age"))
        assertNull(actualResultSet.getString("pronouns"))
        assertEquals(expectedExtra, actualResultSet.getString("extra"))
        verify { messageChannel.createMessage("Awesome! I've overwritten your previous intro, Oliver! :smile:") }
    }

    @Test
    internal fun `should give a prompt to include to correct fields with the intro command when given none`() {
        val slot = slot<String>()
        val expectedHumouringPrompt = "Nothing to be added to your intro!"
        val expectedFormatOfIntroHeader = "To add stuff do it in the following format. (_only name and age are required, any order_):"
        val expectedFormatOfIntro = "name=YOUR NAME,age=YOUR AGE,pronouns=YOUR PRONOUNS,extra=YOUR EXTRA NOTES"
        whenHandlerIsInvokedAsAuthorWithMessage("33332", "!intro")
        verify { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains(expectedHumouringPrompt))
        assertTrue(actualMessage.contains(expectedFormatOfIntroHeader))
        assertTrue(actualMessage.contains(expectedFormatOfIntro))
    }

    private fun whenHandlerIsInvokedAsAuthorWithMessage(authorId: String, messageContent: String) {
        every { message.content } returns Optional.of(messageContent)
        every { author.id } returns Snowflake.of(authorId)
        introHandler.accept(messageCreateEvent)
    }

    private fun getFirstRowOfQuery(): ResultSet {
        val queryIntroTableSql = collectFilePathData("query_intro_table.sql")
        return h2Connection.createStatement().executeQuery(queryIntroTableSql).apply { next() }
    }
}
