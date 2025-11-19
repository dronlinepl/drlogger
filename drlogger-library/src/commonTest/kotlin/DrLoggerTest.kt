import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import pl.dronline.utils.log.DrLogger
import pl.dronline.utils.log.ILogListener
import pl.dronline.utils.log.listener.EmojiConsoleLogListener
import kotlin.test.Test

class DrLoggerTest {
    @Test
    fun test() {
        runBlocking {
            DrLogger.addListener(EmojiConsoleLogListener().apply { enabled = true })

            println(DrLogger.listeners)

            delay(1000)

            DrLogger("TEST").fatal("TEST 1 FATAL")
            DrLogger("TEST").info("TEST 2 INFO")
            DrLogger("TEST").error("❤\uFE0F TEST 3 ERROR")
            DrLogger("TEST").trace("TEST 4 TRACE")
            DrLogger("TEST").error(Exception("TEST"), "TEST 5 EXCEPTION")

            delay(1000)
        }
    }

    @Test
    fun test2() {
        runBlocking {
            println("------------------------------------------------")
            DrLogger.addListener(EmojiConsoleLogListener().apply {
                enabled = true
                minLevel = ILogListener.Level.WARN
            })

            println(DrLogger.listeners)

            delay(1000)

            DrLogger("TEST2").info("TEST INFO")
            DrLogger("TEST2").warn("TEST WARNING")
            DrLogger("TEST2").error("TEST ERROR")
            DrLogger("TEST2").debug("TEST DEBUG")

            DrLogger("TEST2").error(Exception("TEST2"), "TEST EXCEPTION")

            delay(1000)
        }
    }

    @Test
    fun test3() {
        runBlocking {
            DrLogger.addListener(EmojiConsoleLogListener().apply {
                enabled = true
                minLevel = ILogListener.Level.DEBUG
            }
            )

            println(DrLogger.listeners)

            delay(1000)

            val logger = DrLogger("TEST3")

            logger.d("ABCD.100.020", "Debug test log message")
            delay(10)
            logger.t("ABCD.100.010", "Trace test log message")
            delay(10)
            logger.i("ABCD.100.030", "Info test log message")
            delay(10)
            logger.i("ABCD.100.040", Exception("Ex INFO"), "Info test log message with exception")
            delay(10)
            logger.w("ABCD.100.050", "Warn test log message")
            delay(10)
            logger.w("ABCD.100.060", Exception("Ex WARN"), "Warn test log message with Exception")
            delay(10)
            logger.e("ABCD.100.101", "❤\uFE0F Error test message with color")
            delay(10)
            logger.e("ABCD.100.102", Exception("EX ERROR"), "Error test log exception with Exception")

            delay(1000)
        }
    }
}