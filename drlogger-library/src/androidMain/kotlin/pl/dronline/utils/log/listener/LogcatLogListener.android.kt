package pl.dronline.utils.log.listener

import android.util.Log
import kotlinx.datetime.Instant
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.ILogListener

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class LogcatLogListener : ALogListener("LogcatLogListener"), ILogListener {

    actual override var enabled: Boolean = true

    private val regex = "[\n\r\t]".toRegex()

    actual override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    ) {

        val chunkSize = 3900
        val data = message.replace(regex, "").trim()

        if (data.length > chunkSize) {
            data.chunked(chunkSize).let { chunked ->

                val msgParts = chunked.size.toString()
                chunked.forEachIndexed { j, s ->
                    log(
                        level, type, "[${
                            (j + 1).toString()
                                .padStart(2, '0')
                        }/${msgParts.padStart(2, '0')}] $s", null
                    )
                }
            }
        } else {
            log(level, type, data, t)
        }
    }

    private fun log(level: ILogListener.Level, type: String, message: String, t: Throwable?) {
        when (level) {
            ILogListener.Level.DEBUG -> {
                if (t != null) Log.d(type, message, t) else Log.d(type, message)
            }

            ILogListener.Level.TRACE -> {
                if (t != null) Log.v(type, message, t) else Log.v(type, message)
            }

            ILogListener.Level.INFO -> {
                if (t != null) Log.i(type, message, t) else Log.i(type, message)
            }

            ILogListener.Level.WARN -> {
                if (t != null) Log.w(type, message, t) else Log.w(type, message)
            }

            ILogListener.Level.ERROR -> {
                if (t != null) Log.e(type, message, t) else Log.e(type, message)
            }

            ILogListener.Level.FATAL -> {
                if (t != null) Log.wtf(type, message, t) else Log.wtf(type, message)
            }
        }
    }
}