package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.sql.updateUsingResource
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
        whoIsHandler = WhoIsHandler(h2Connection)
        h2Connection.updateUsingResource("create_intro_table.sql")
    }

    @AfterEach
    internal fun tearDown() {
        h2Connection.updateUsingResource("delete_intro_table.sql")
    }

    @Test
    internal fun `should respond with a message containing intro data for a mention that has an intro`() {
        val expectedDiscordId = "7777777"
        val expectedName = "Larissa"
        val expectedAge = "19"
        val expectedPronouns = "she/her"
        val expectedExtra = "i love bunnies"
        val slot = slot<String>()
        givenExpectedInPostgres(expectedDiscordId, expectedName, expectedAge, expectedPronouns, expectedExtra)
        every { message.userMentionIds } returns setOf(Snowflake.of(expectedDiscordId))
        whoIsHandler.accept(messageCreateEvent)
        verify { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: $expectedName"))
        assertTrue(actualMessage.contains("age: $expectedAge"))
        assertTrue(actualMessage.contains("pronouns: $expectedPronouns"))
        assertTrue(actualMessage.contains("extra: $expectedExtra"))
    }

    @Test
    internal fun `should only respond with the fields populated fields when only the required fields have values`() {
        val expectedDiscordId = "2839182"
        val expectedName = "Alfred"
        val expectedAge = "1029"
        val slot = slot<String>()
        givenExpectedInPostgresOfOnlyRequiredFields(expectedDiscordId, expectedName, expectedAge)
        every { message.userMentionIds } returns setOf(Snowflake.of(expectedDiscordId))
        whoIsHandler.accept(messageCreateEvent)
        verify { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: $expectedName"))
        assertTrue(actualMessage.contains("age: $expectedAge"))
        assertFalse(actualMessage.contains("pronouns"))
        assertFalse(actualMessage.contains("extra"))
    }

    @Test
    internal fun `should respond with only intro when one of the mentions has no intro`() {
        val expectedDiscordId = "2839183"
        val expectedName = "Candi"
        val expectedAge = "-1"
        val slot = slot<String>()
        givenExpectedInPostgresOfOnlyRequiredFields(expectedDiscordId, expectedName, expectedAge)
        every { message.userMentionIds } returns setOf(Snowflake.of(expectedDiscordId), Snowflake.of("1234"))
        whoIsHandler.accept(messageCreateEvent)
        verify(exactly = 1) { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: $expectedName"))
        assertTrue(actualMessage.contains("age: $expectedAge"))
        assertFalse(actualMessage.contains("pronouns"))
        assertFalse(actualMessage.contains("extra"))
    }

    private fun givenExpectedInPostgresOfOnlyRequiredFields(expectedDiscordId: String, expectedName: String, expectedAge: String) {
        val macroMap = mapOf(
            ":(discord_id)" to expectedDiscordId,
            ":(name)" to expectedName,
            ":(age)" to expectedAge
        )
        h2Connection.updateUsingResource("insert_intro_data.sql", macroMap)
    }

    private fun givenExpectedInPostgres(expectedDiscordId: String, expectedName: String, expectedAge: String, expectedPronouns: String, expectedExtra: String) {
        val macroMap = mapOf(
            ":(discord_id)" to expectedDiscordId,
            ":(name)" to expectedName,
            ":(age)" to expectedAge,
            ":(pronouns)" to expectedPronouns,
            ":(extra)" to expectedExtra
        )
        h2Connection.updateUsingResource("insert_intro_data.sql", macroMap)
    }
}
