package fish.eyebrow.bunnybot.module

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import discord4j.core.DiscordClient
import discord4j.core.DiscordClientBuilder
import fish.eyebrow.bunnybot.CommandLineArguments
import fish.eyebrow.bunnybot.handler.DiscordClientWrapper
import fish.eyebrow.bunnybot.handler.IntroHandler
import fish.eyebrow.bunnybot.handler.WhoIsHandler
import java.sql.Connection
import java.sql.DriverManager

class BunnyBotModule(private val commandLineArguments: CommandLineArguments) : AbstractModule() {
    @Inject
    @Provides
    @Singleton
    fun createDiscordClient(): DiscordClient = DiscordClientBuilder(commandLineArguments.token!!).build()

    @Inject
    @Provides
    @Singleton
    fun createPostgresConnection(): Connection =
            if (commandLineArguments.dbUser == null || commandLineArguments.dbPassword == null)
                DriverManager.getConnection(commandLineArguments.dbUrl!!)
            else
                DriverManager.getConnection(commandLineArguments.dbUrl!!, commandLineArguments.dbUser, commandLineArguments.dbPassword)

    @Inject
    @Provides
    @Singleton
    fun createIntroHandler(connection: Connection): IntroHandler =
            IntroHandler(connection)

    @Inject
    @Provides
    @Singleton
    fun createWhoIsHandler(connection: Connection): WhoIsHandler = WhoIsHandler(connection)

    @Inject
    @Provides
    @Singleton
    fun createDiscordClientWrapper(discordClient: DiscordClient, introHandler: IntroHandler, whoIsHandler: WhoIsHandler): DiscordClientWrapper =
            DiscordClientWrapper(discordClient, introHandler, whoIsHandler)
}