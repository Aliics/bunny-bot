package fish.eyebrow.bunnybot.handler

import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import fish.eyebrow.bunnybot.dao.IntroDao
import fish.eyebrow.bunnybot.model.Intro
import fish.eyebrow.bunnybot.util.collectFilePathData
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
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
    @MockK
    private lateinit var guild: Guild
    @MockK
    private lateinit var member: Member
    private lateinit var messageCreateEvent: MessageCreateEvent
    private lateinit var h2Connection: Connection
    private lateinit var whoIsHandler: WhoIsHandler

    @BeforeEach
    internal fun setUp() {
        every { message.author } returns Optional.of(author)
        every { message.channel } returns Mono.just(messageChannel)
        every { message.guild } returns Mono.just(guild)
        every { messageChannel.createMessage(any<String>()) } returns Mono.just(message)
        every { guild.getMemberById(any()) } returns Mono.just(member)
        every { member.mention } returns ""
        messageCreateEvent = MessageCreateEvent(discordClient, message, null, null)
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        whoIsHandler = WhoIsHandler(IntroDao(h2Connection))
        h2Connection.createStatement().executeUpdate(collectFilePathData("create_intro_table.sql"))
    }

    @AfterEach
    internal fun tearDown() {
        try {
            h2Connection.createStatement().executeUpdate(collectFilePathData("delete_intro_table.sql"))
        } catch (e: Exception) {
        }
    }

    @Test
    internal fun `should respond with a message containing intro data for a mention that has an intro`() {
        val slot = slot<String>()
        val expectedIntro = Intro(discordId = "7777777", name = "Larissa", age = "19", pronouns = "she/her", extra = "i loves bunnies")
        givenExpectedInPostgres(expectedIntro)
        whenMessageEventIsCapturedWithSetOfMentions(setOf(Snowflake.of(expectedIntro.discordId)))
        verify { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: ${expectedIntro.name}"))
        assertTrue(actualMessage.contains("age: ${expectedIntro.age}"))
        assertTrue(actualMessage.contains("pronouns: ${expectedIntro.pronouns}"))
        assertTrue(actualMessage.contains("extra: ${expectedIntro.extra}"))
    }

    @Test
    internal fun `should only respond with nulled out fields when intro does not contain fields`() {
        val slot = slot<String>()
        val expectedIntro = Intro(discordId = "2839182", name = "Alfred", age = "1029")
        givenExpectedInPostgres(expectedIntro)
        whenMessageEventIsCapturedWithSetOfMentions(setOf(Snowflake.of(expectedIntro.discordId)))
        verify { messageChannel.createMessage(capture(slot)) }
        val actualMessage = slot.captured
        assertTrue(actualMessage.contains("name: ${expectedIntro.name}"))
        assertTrue(actualMessage.contains("age: ${expectedIntro.age}"))
        assertTrue(actualMessage.contains("pronouns: null"))
        assertTrue(actualMessage.contains("extra: null"))
    }

    @Test
    internal fun `should respond with no intro found when mentioned has no intro`() {
        val expectedMention = "<@Candi#1111>"
        val givenSnowflake = Snowflake.of("11112222")
        every { member.mention } returns expectedMention
        whenMessageEventIsCapturedWithSetOfMentions(setOf(givenSnowflake))
        verify { messageChannel.createMessage("Uh oh! $expectedMention has no intro yet!") }
    }

    @Test
    internal fun `should prompt to mention someone when no mentions are in content`() {
        whenMessageEventIsCapturedWithSetOfMentions(emptySet())
        verify { messageChannel.createMessage("Oh noes! No mention given with command!") }
    }

    @Test
    internal fun `should prompt that an exception occurred with error message`() {
        givenDroppedIntroTable()
        whenMessageEventIsCapturedWithSetOfMentions(setOf(Snowflake.of("2839182")))
        verify { messageChannel.createMessage("A bizarre error has occurred :alien:") }
    }

    private fun givenDroppedIntroTable() {
        h2Connection.createStatement().executeUpdate(collectFilePathData("delete_intro_table.sql"))
    }

    @Suppress("SameParameterValue")
    private fun givenExpectedInPostgres(intro: Intro) {
        h2Connection.prepareStatement(collectFilePathData("insert_intro_data.sql")).apply {
            setString(1, intro.discordId)
            setString(2, intro.name)
            setString(3, intro.age)
            setString(4, intro.pronouns)
            setString(5, intro.extra)
            executeUpdate()
        }
    }

    private fun whenMessageEventIsCapturedWithSetOfMentions(mentions: Set<Snowflake>) {
        every { message.userMentionIds } returns mentions
        whoIsHandler.accept(messageCreateEvent)
    }
}
