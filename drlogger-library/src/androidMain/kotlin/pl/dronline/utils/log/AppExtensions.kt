package pl.dronline.utils.log

import android.app.Application
import pl.dronline.utils.log.listener.DailyFileLogListener
import pl.dronline.utils.log.listener.LogcatLogListener

/**
 * Enable logcat traces
 */
fun Application.enableLogcat() {
    DrLogger.addListener(LogcatLogListener())
}

/**
 * Enable traces YYYYMMDD.log
 * @path - where files will be stored
 */
fun Application.enableDailyFileLog(
    path: String
) {
    DrLogger.addListener(DailyFileLogListener(this))
}
