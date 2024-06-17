package pl.dronline.utils.log.listener

import kotlinx.datetime.Instant
import pl.dronline.utils.datetime.toString
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.DrLoggerFactory.prepareMessage
import pl.dronline.utils.log.ILogListener
import pl.dronline.utils.log.consoleError
import pl.dronline.utils.log.consolePrint
import platform.Foundation.NSLog

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class LogcatLogListener : ALogListener("LogcatLogListener"), ILogListener {
    companion object {
        private val DEFAULT_EMOIMAP: Map<ILogListener.Level, CharSequence> = mapOf(
            ILogListener.Level.FATAL to "\u2620\uFE0F",
            ILogListener.Level.ERROR to "❤\uFE0F",
            ILogListener.Level.WARN to "\uD83E\uDDE1",
            ILogListener.Level.INFO to "\uD83D\uDC99",
            ILogListener.Level.DEBUG to "\uD83E\uDD0D",
            ILogListener.Level.TRACE to "\uD83D\uDC9C",
        )
    }

    val logLevelMap = mutableMapOf<ILogListener.Level, CharSequence>().apply {
        putAll(DEFAULT_EMOIMAP)
    }

    actual override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    ) {
        val sb = buildString {
            append(logLevelMap[level])
            append(" ")
            append(prepareMessage(type, message, t))
        }
        NSLog(sb)
    }

    actual override var enabled: Boolean = true
}