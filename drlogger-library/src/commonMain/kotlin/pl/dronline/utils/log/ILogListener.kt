/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Interface defining a log listener for the DrLogger system.
 * A log listener is responsible for handling log messages and writing them to their respective output.
 * Implementations may include console loggers, file loggers, or platform-specific loggers.
 */
interface ILogListener {

    /**
     * Defines the available log levels in ascending order of severity.
     *
     * DEBUG: Detailed information for debugging purposes only.
     * TRACE: More detailed than DEBUG, used for tracing program execution.
     * INFO: General information about application progress.
     * WARN: Potentially harmful situations that might lead to errors.
     * ERROR: Error events that might still allow the application to continue running.
     * FATAL: Critical errors that will likely lead to application termination.
     *        Used for catastrophic failures that require immediate attention.
     */
    enum class Level(val value: Int) {
        DEBUG(0),
        TRACE(1),
        INFO(2),
        WARN(4),
        ERROR(8),
        FATAL(16)
    }


    /**
     * Writes a log message to the implemented output destination.
     *
     * @param timestamp The time when the log event occurred
     * @param level The severity level of the log message
     * @param type The tag or category of the log message, typically a class name or module
     * @param message The content of the log message
     * @param t Optional throwable associated with the log message, useful for error reporting
     */
    @OptIn(ExperimentalTime::class)
    fun writeLog(
        timestamp: Instant,
        level: Level,
        type: String,
        message: String,
        t: Throwable? = null
    )

    /**
     * The name of this log listener instance, used for identification and management
     */
    val name: String

    /**
     * Controls whether this log listener is active and processing log messages
     */
    var enabled: Boolean

    /**
     * The minimum log level this listener will process. Messages with a lower level will be ignored.
     */
    var minLevel: Level

    /**
     * Optional regex pattern to filter log messages by content.
     * Only messages matching this pattern will be processed.
     */
    var messageRegex : Regex?

    /**
     * Optional regex pattern to filter log messages by tag.
     * Only messages with tags matching this pattern will be processed.
     */
    var tagRegex : Regex?

    /**
     * Starts listening to the log flow.
     * Each listener implementation should handle its own subscription and filtering.
     */
    fun startListening(scope: CoroutineScope): Job

    /**
     * Stops listening to the log flow.
     */
    fun stopListening()
}