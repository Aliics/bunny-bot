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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Mono
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

@ExtendWith(MockKExtension::class)
internal class WhoIsHandlerTestCase {
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
    private lateinit var whoIsHandler: WhoIsHandler

    @BeforeEach
    internal fun setUp() {
        every { message.author } returns Optional.of(author)
        every { message.channel } returns Mono.just(messageChannel)
        every { messageChannel.createMessage(any<String>()) } returns Mono.just(message)
        messageCreateEvent = MessageCreateEvent(discordClient, message, null, null)
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        whoIsHandler = WhoIsHandler(IntroDao(h2Connection))
        h2Connection.createStatement().executeUpdate(collectFilePathData("create_intro_table.sql"))
    }

    @AfterEach
    internal fun tearDown() {
        h2Connection.createStatement().executeUpdate(collectFilePathData("delete_intro_table.sql"))
    }

    @Test
    internal fun `should respond with a message containing intro data for a mention that has an intro`() {
        val slot = slot<String>()
        val expectedDiscordId = "7777777"
        val expectedName = "Larissa"
        val expectedAge = "19"
        val expectedPronouns = "she/her"
        val expectedExtra = "i love bunnies"
        givenExpectedInPostgres(expectedDiscordId, expectedName, expectedAge, expectedPronouns, expectedExtra)
        whenMessageEventIsCapturedWithSetOfMentions(setOf(Snowflake.of(expectedDiscordId)))
        verify { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: $expectedName"))
        assertTrue(actualMessage.contains("age: $expectedAge"))
        assertTrue(actualMessage.contains("pronouns: $expectedPronouns"))
        assertTrue(actualMessage.contains("extra: $expectedExtra"))
    }

    @Test
    internal fun `should only respond with the fields populated fields when only the required fields have values`() {
        val slot = slot<String>()
        val expectedDiscordId = "2839182"
        val expectedName = "Alfred"
        val expectedAge = "1029"
        givenExpectedInPostgresOfOnlyRequiredFields(expectedDiscordId, expectedName, expectedAge)
        whenMessageEventIsCapturedWithSetOfMentions(setOf(Snowflake.of(expectedDiscordId)))
        verify { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: $expectedName"))
        assertTrue(actualMessage.contains("age: $expectedAge"))
        assertFalse(actualMessage.contains("pronouns"))
        assertFalse(actualMessage.contains("extra"))
    }

    @Test
    internal fun `should respond with only intro when one of the mentions has no intro`() {
        val slot = slot<String>()
        val expectedDiscordId = "2839183"
        val expectedName = "Candi"
        val expectedAge = "-1"
        givenExpectedInPostgresOfOnlyRequiredFields(expectedDiscordId, expectedName, expectedAge)
        whenMessageEventIsCapturedWithSetOfMentions(setOf(Snowflake.of(expectedDiscordId), Snowflake.of("1234")))
        verify(exactly = 1) { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: $expectedName"))
        assertTrue(actualMessage.contains("age: $expectedAge"))
        assertFalse(actualMessage.contains("pronouns"))
        assertFalse(actualMessage.contains("extra"))
    }

    @Test
    internal fun `should prompt to mention someone when no mentions are in content`() {
        whenMessageEventIsCapturedWithSetOfMentions(emptySet())
        verify { messageChannel.createMessage("Oh noes! No mention given with command!") }
    }

    private fun givenExpectedInPostgresOfOnlyRequiredFields(expectedDiscordId: String, expectedName: String, expectedAge: String) {
        h2Connection.prepareStatement(collectFilePathData("insert_intro_data.sql")).apply {
            setString(1, expectedDiscordId)
            setString(2, expectedName)
            setString(3, expectedAge)
            setString(4, null)
            setString(5, null)
            executeUpdate()
        }
    }

    @Suppress("SameParameterValue")
    private fun givenExpectedInPostgres(
            expectedDiscordId: String,
            expectedName: String,
            expectedAge: String,
            expectedPronouns: String,
            expectedExtra: String
    ) {
        h2Connection.prepareStatement(collectFilePathData("insert_intro_data.sql")).apply {
            setString(1, expectedDiscordId)
            setString(2, expectedName)
            setString(3, expectedAge)
            setString(4, expectedPronouns)
            setString(5, expectedExtra)
            executeUpdate()
        }
    }

    private fun whenMessageEventIsCapturedWithSetOfMentions(mentions: Set<Snowflake>) {
        every { message.userMentionIds } returns mentions
        whoIsHandler.accept(messageCreateEvent)
    }
}
