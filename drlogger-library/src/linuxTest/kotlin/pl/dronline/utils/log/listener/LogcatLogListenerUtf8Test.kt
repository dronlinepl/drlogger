package pl.dronline.utils.log.listener

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class LogcatLogListenerUtf8Test {
    @Test
    fun syslogPayload_nonAsciiMessage_preservesAllUtf8Bytes() {
        val message = "ąęć koniec\n"

        val payload = syslogPayload(message)

        assertEquals(14, payload.size)
        assertContentEquals(message.encodeToByteArray(), payload)
        assertEquals(message, payload.decodeToString())
    }
}
