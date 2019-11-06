package fish.eyebrow.bunnybot.module

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import fish.eyebrow.bunnybot.CommandLineArguments
import fish.eyebrow.bunnybot.handler.DiscordClientWrapper
import fish.eyebrow.bunnybot.handler.EchoHandler

class BunnyBotModule(private val commandLineArguments: CommandLineArguments) : AbstractModule() {
    @Inject
    @Provides
    fun createDiscordClient(): DiscordClient = DiscordClientBuilder(commandLineArguments.token!!).build()

    @Inject
    @Provides
    fun createEchoHandler(discordClient: DiscordClient) = EchoHandler(discordClient)

    @Inject
    @Provides
    fun createHandlerDelegate(discordClient: DiscordClient, echoHandler: EchoHandler) = DiscordClientWrapper(discordClient, echoHandler)
}