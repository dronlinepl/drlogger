/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log.listener

import pl.dronline.utils.datetime.toString
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.DrLoggerFactory.prepareMessage
import pl.dronline.utils.log.ILogListener
import pl.dronline.utils.log.consoleError
import pl.dronline.utils.log.consolePrint
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A log listener implementation that outputs log messages to the console with emoji indicators.
 * Each log level is represented by a different emoji, making logs visually distinguishable.
 *
 * This listener uses different colored heart emojis for different log levels, with the
 * exception of the FATAL level which uses a skull and crossbones emoji to indicate critical errors.
 */
class EmojiConsoleLogListener : ALogListener("EmojiConsoleLogListener") {
    override var enabled: Boolean = true

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

    /**
     * A mutable map of log levels to emoji characters.
     * This map can be modified at runtime to customize the emoji for each log level.
     */
    val logLevelMap = mutableMapOf<ILogListener.Level, CharSequence>().apply {
        putAll(DEFAULT_EMOIMAP)
    }

    /**
     * Writes a log message to the console with an emoji indicator corresponding to the log level.
     *
     * The format of the log message is: timestamp followed by a colored heart emoji, then the message.
     * Each log level uses a different colored heart emoji (red, orange, blue, white, purple),
     * except for FATAL level which uses a skull emoji to indicate critical errors.
     *
     * For ERROR and FATAL level messages, the output is directed to the error console.
     * For all other levels, the output is directed to the standard console.
     *
     * @param timestamp The time when the log event occurred
     * @param level The severity level of the log message
     * @param type The tag or category of the log message
     * @param message The content of the log message
     * @param t Optional throwable associated with the log message
     */
    @OptIn(ExperimentalTime::class)
    override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    ) {
        val sb = buildString {
            append(timestamp.toString("HH:mm:ss.SSS"))
            append(" ")
            append(logLevelMap[level])
            append(" ")
            append(prepareMessage(type, message, t))
        }
        if (level == ILogListener.Level.ERROR || level == ILogListener.Level.FATAL) {
            consoleError(sb)
        } else {
            consolePrint(sb)
        }
    }
}