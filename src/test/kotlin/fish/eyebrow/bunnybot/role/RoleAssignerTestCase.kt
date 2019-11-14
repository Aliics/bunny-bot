package fish.eyebrow.bunnybot.role

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.util.Snowflake
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ExtendWith(MockKExtension::class)
internal class RoleAssignerTestCase {
    @MockK
    private lateinit var message: Message
    @MockK
    private lateinit var member: Member
    @MockK
    private lateinit var guild: Guild
    @MockK
    private lateinit var messageChannel: MessageChannel
    @MockK
    private lateinit var role: Role

    @BeforeEach
    internal fun setUp() {
        every { message.authorAsMember } returns Mono.just(member)
        every { message.guild } returns Mono.just(guild)
        every { message.channel } returns Mono.just(messageChannel)
        every { messageChannel.createMessage(any<String>()) } returns Mono.empty()
        every { guild.roles } returns Flux.fromArray(arrayOf(role))
    }

    @Test
    internal fun `should assign adult age role to member when given a numeric age`() {
        val roleId = Snowflake.of(1L)
        givenRoleOfNameAndId("bunnies", roleId)
        every { member.addRole(any()) } returns Mono.empty()
        RoleAssigner.assignAge("20", message)
        verify { member.addRole(roleId) }
        verify { messageChannel.createMessage("You've also been assigned to the _bunnies_ role!") }
    }

    @Test
    internal fun `should assign minor age role to member when given a numeric age below 18`() {
        val roleId = Snowflake.of(1L)
        givenRoleOfNameAndId("lil bunnies", roleId)
        every { member.addRole(any()) } returns Mono.empty()
        RoleAssigner.assignAge("15", message)
        verify { member.addRole(roleId) }
        verify { messageChannel.createMessage("You've also been assigned to the _lil bunnies_ role!") }
    }

    @Test
    internal fun `should assign adult age role to member when given adult string`() {
        val roleId = Snowflake.of(1L)
        givenRoleOfNameAndId("bunnies", roleId)
        every { member.addRole(any()) } returns Mono.empty()
        RoleAssigner.assignAge("adult", message)
        verify { member.addRole(roleId) }
        verify { messageChannel.createMessage("You've also been assigned to the _bunnies_ role!") }
    }

    @Test
    internal fun `should assign minor age role to member when given minor string`() {
        val roleId = Snowflake.of(1L)
        givenRoleOfNameAndId("lil bunnies", roleId)
        every { member.addRole(any()) } returns Mono.empty()
        RoleAssigner.assignAge("minor", message)
        verify { member.addRole(roleId) }
        verify { messageChannel.createMessage("You've also been assigned to the _lil bunnies_ role!") }
    }

    @Test
    internal fun `should not assign age role to member when given random string`() {
        val roleId = Snowflake.of(1L)
        givenRoleOfNameAndId("lil bunnies", roleId)
        every { member.addRole(any()) } returns Mono.empty()
        RoleAssigner.assignAge("m1n0r", message)
        verify(exactly = 0) { member.addRole(roleId) }
        verify(exactly = 0) { messageChannel.createMessage(any<String>()) }
    }

    private fun givenRoleOfNameAndId(name: String, id: Snowflake) {
        every { role.name } returns name
        every { role.id } returns id
    }
}
