import pl.dronline.utils.log.ILogListener
import pl.dronline.utils.log.listener.LogcatLogListener
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Reproduces a TestFlight crash where the app dies inside NSLog →
 * __CFStringAppendFormatCore → _platform_strlen when the log message
 * contains '%' characters (JSON payloads, "%s" template strings, URL
 * encodings).
 *
 * Cause: LogcatLogListener.apple.kt passed the message to NSLog as the
 * format string, so CoreFoundation treated every '%' as a conversion
 * specifier and pulled random varargs that don't exist. With the fix
 * (NSLog("%s", msg.cstr.ptr) inside memScoped) this test runs to
 * completion.
 *
 * Note: writeLog is called directly (synchronously) — no flow plumbing —
 * so the crash, if any, surfaces on the test thread and the test process
 * aborts. A passing run means the bug is gone.
 */
@OptIn(ExperimentalTime::class)
class LogcatLogListenerFormatCrashTest {

    @Test
    fun writeLogDoesNotCrashOnPercentInMessage() {
        val listener = LogcatLogListener()
        val now = Clock.System.now()
        val messages = listOf(
            "plain message",
            "json payload {\"k\":\"v\"}",
            "looks like printf: %s %d %@",
            "url encoded path /api?q=%20%2Fhello",
            "percent literal 50%",
            "stacking %%%% and %s",
        )
        for (msg in messages) {
            listener.writeLog(
                timestamp = now,
                level = ILogListener.Level.INFO,
                type = "TEST",
                message = msg,
                t = null,
            )
        }
    }
}