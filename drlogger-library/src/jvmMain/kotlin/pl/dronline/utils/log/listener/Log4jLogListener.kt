package pl.dronline.utils.log.listener

import kotlinx.datetime.Instant
import org.apache.logging.log4j.LogManager
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.ILogListener
import java.io.PrintWriter
import java.io.StringWriter

class Log4jLogListener : ALogListener("Log4jLogListener"), ILogListener {
    override fun writeLog(timestamp: Instant, level: ILogListener.Level, type: String, message: String, t: Throwable?) {
        val log = LogManager.getLogger(type)

        val sb = StringBuilder()

        try {
            sb.append(message)

            // log throwable
            if (t != null) {
                sb.append("\n")
                StringWriter().use { errors ->
                    PrintWriter(errors).use { printWriter ->
                        t.printStackTrace(printWriter)
                    }
                    sb.append(errors.toString())
                }
            }

        } finally {

            if (sb.isNotEmpty()) {
                when (level) {
                    ILogListener.Level.DEBUG -> log.debug(sb.toString())
                    ILogListener.Level.TRACE -> log.trace(sb.toString())
                    ILogListener.Level.INFO -> log.info(sb.toString())
                    ILogListener.Level.WARN -> log.warn(sb.toString())
                    ILogListener.Level.ERROR -> log.error(sb.toString())
                    ILogListener.Level.FATAL -> log.fatal(sb.toString())
                }
            }
        }
    }

    override var enabled: Boolean = true
}