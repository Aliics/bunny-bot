package fish.eyebrow.bunnybot.module

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import fish.eyebrow.bunnybot.CommandLineArguments
import fish.eyebrow.bunnybot.handler.DiscordClientWrapper

class BunnyBotModule(private val commandLineArguments: CommandLineArguments) : AbstractModule() {
    @Inject
    @Provides
    @Singleton
    fun createDiscordClient(): DiscordClient = DiscordClientBuilder(commandLineArguments.token!!).build()

    @Inject
    @Provides
    @Singleton
    fun createDiscordClientWrapper(discordClient: DiscordClient) = DiscordClientWrapper(discordClient)
}