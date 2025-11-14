import pl.dronline.utils.log.DrLogger
import pl.dronline.utils.log.ILogListener
import pl.dronline.utils.log.listener.DailyFileLogListener
import pl.dronline.utils.log.listener.EmojiConsoleLogListener
import pl.dronline.utils.log.listener.Log4jLogListener
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class DrLoggerTestJvm {
    @Test
    @OptIn(ExperimentalTime::class)
    fun test() {
        DrLogger.addListener(
            Log4jLogListener().apply {
                enabled = true
            },
            EmojiConsoleLogListener().apply {
                enabled = true
                logLevelMap[ILogListener.Level.INFO] = "[1INFO]"
                logLevelMap[ILogListener.Level.ERROR] = "[1ERROR]"
                logLevelMap[ILogListener.Level.ERROR] = "[1DEBUG]"
                logLevelMap[ILogListener.Level.ERROR] = "[1WARN]"
                tagRegex = "^TEST.*".toRegex()
            },
            EmojiConsoleLogListener().apply {
                enabled = true
                logLevelMap[ILogListener.Level.INFO] = "[2INFO]"
                logLevelMap[ILogListener.Level.ERROR] = "[2ERROR]"
                logLevelMap[ILogListener.Level.ERROR] = "[2DEBUG]"
                logLevelMap[ILogListener.Level.ERROR] = "[2WARN]"
                tagRegex = "^TES.*".toRegex()
                messageRegex = "^info.*".toRegex()
            },
            DailyFileLogListener().apply {
                enabled = true
                path = "/tmp"
                tagRegex = "^TEST.*".toRegex()
                namePrefix = "test_"
            })


        Thread.sleep(1000)

        println("LISTENERS: ${pl.dronline.utils.log.DrLogger.listeners}")

        DrLogger("TEST").info { "info" }
        DrLogger("TESA").info { "info" }
        DrLogger("TEST").info { "inxo2" }
        DrLogger("TESA").info { "info2" }

        Thread.sleep(1000)
    }
}