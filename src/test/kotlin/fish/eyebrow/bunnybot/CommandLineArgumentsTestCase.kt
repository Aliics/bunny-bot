package fish.eyebrow.bunnybot

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class CommandLineArgumentsTestCase {
    @Test
    internal fun `should populate token when passed with option`() {
        val args = arrayOf("-token=ead74f3c-827f-4ce8-ac3b-929d63fb75aa")
        val expectedToken = "ead74f3c-827f-4ce8-ac3b-929d63fb75aa"
        val actualToken = CommandLineParser.parse(args).token
        assertEquals(expectedToken, actualToken)
    }

    @Test
    fun `should not set token when no option is passed`() {
        val args = emptyArray<String>()
        val actualToken = CommandLineParser.parse(args).token
        assertNull(actualToken)
    }
}
