package fish.eyebrow.bunnybot.handler

import java.sql.DriverManager
import kotlin.test.BeforeTest

internal class IntroHandlerTestCase {
    private lateinit var introHandler: IntroHandler

    @BeforeTest
    internal fun setUp() {
        val h2Connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        introHandler = IntroHandler(h2Connection)
    }
}
