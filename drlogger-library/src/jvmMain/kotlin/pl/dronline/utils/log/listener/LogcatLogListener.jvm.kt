package pl.dronline.utils.log.listener

import kotlinx.datetime.Instant
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.ILogListener

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class LogcatLogListener : ALogListener("LogcatLogListener"), ILogListener {
    actual override fun writeLog(timestamp: Instant, level: ILogListener.Level, type: String, message: String, t: Throwable?) {
        error("This platform does not support Logcat")
    }
    actual override var enabled: Boolean = false
}