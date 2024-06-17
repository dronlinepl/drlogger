package pl.dronline.utils.log.listener

import kotlinx.cinterop.*
import kotlinx.datetime.Instant
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.DrLoggerFactory.prepareMessage
import pl.dronline.utils.log.ILogListener
import platform.linux.sockaddr_un
import platform.posix.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class LogcatLogListener : ALogListener("LogcatLogListener"), ILogListener {
    companion object {
        private val mapLevel = mapOf(
            ILogListener.Level.ERROR to 3,
            ILogListener.Level.WARN to 4,
            ILogListener.Level.INFO to 6,
            ILogListener.Level.DEBUG to 7,
            ILogListener.Level.TRACE to 7,
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    ) {
        val sb = StringBuilder()
        sb.append(prepareMessage("[$type]", message, t))
        val logSocket = socket(AF_UNIX, SOCK_DGRAM, 0)
        if (logSocket < 0) {
            perror("socket failed")
            return
        }

        try {
            memScoped {
                val logAddr = alloc<sockaddr_un>()
                memset(logAddr.ptr, 0, sizeOf<sockaddr_un>().convert())

                logAddr.sun_family = AF_UNIX.convert()
                strncpy(logAddr.sun_path, "/dev/log", sizeOf<sockaddr_un>().convert())

                val fullMessage = "<${mapLevel[level]}> $sb"

                if (connect(logSocket, logAddr.ptr.reinterpret(), sizeOf<sockaddr_un>().convert()) < 0) {
                    perror("connect failed")
                    return
                }

                if (send(logSocket, fullMessage.cstr, fullMessage.length.convert(), 0) < 0) {
                    perror("send failed")
                }
            }
        } finally {
            close(logSocket)
        }
    }

    actual override var enabled: Boolean = true
}