/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log.listener

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import pl.dronline.utils.datetime.toString
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.DrLoggerFactory.prepareMessage
import pl.dronline.utils.log.ILogListener
import pl.dronline.utils.log.consoleError
import pl.dronline.utils.log.consolePrint
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import platform.Foundation.NSLog

@ExperimentalTime
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

    @OptIn(ExperimentalForeignApi::class)
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
        // Must NOT pass the message as the format string — any '%' in the
        // log content (JSON, "%s", URL-encoded paths) is otherwise treated
        // as a conversion specifier and consumes nonexistent varargs,
        // crashing CoreFoundation in __CFStringAppendFormatCore /
        // _platform_strlen. Kotlin/Native doesn't auto-bridge a Kotlin
        // String (or even an NSString built from it) through ObjC variadic
        // args, so going via .cstr under "%s" is the reliable path.
        memScoped {
            NSLog("%s", sb.cstr.ptr)
        }
    }

    actual override var enabled: Boolean = true
}