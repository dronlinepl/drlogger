/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log.listener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import pl.dronline.utils.log.ILogListener
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A log listener implementation that writes log messages to daily rotating log files.
 * This listener creates a new log file each day, organizing logs chronologically
 * while preventing individual log files from growing too large.
 *
 * Features:
 * - Daily log rotation
 * - Automatic cleanup of old log files on startup
 * - Maximum file count limit
 * - Maximum file age limit
 *
 * This is an expect class that is implemented differently on each supported platform,
 * as file handling is platform-specific.
 */
@OptIn(ExperimentalTime::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DailyFileLogListener() : ILogListener {
    /**
     * Writes a log message to the current day's log file.
     *
     * @param timestamp The time when the log event occurred
     * @param level The severity level of the log message
     * @param type The tag or category of the log message
     * @param message The content of the log message
     * @param t Optional throwable associated with the log message
     */

    override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    )

    /**
     * The name of this log listener instance
     */
    override val name: String

    /**
     * Controls whether this log listener is active and processing log messages
     */
    override var enabled: Boolean

    /**
     * The minimum log level this listener will process
     * Messages with a lower level will be ignored
     */
    override var minLevel: ILogListener.Level

    /**
     * Optional regex pattern to filter log messages by content
     * Only messages matching this pattern will be processed
     */
    override var messageRegex: Regex?

    /**
     * Optional regex pattern to filter log messages by tag
     * Only messages with tags matching this pattern will be processed
     */
    override var tagRegex: Regex?

    /**
     * The directory path where log files will be stored
     * If null, a default platform-specific location will be used
     */
    var path: String?

    /**
     * The prefix to be added to log filenames
     * The full filename format will be: [namePrefix]YYYY-MM-DD.log
     */
    var namePrefix: String

    /**
     * Maximum number of log files to retain.
     * Older files will be deleted when this limit is exceeded.
     * Default: 30 (approximately 1 month of daily logs)
     */
    var maxFileCount: Int

    /**
     * Maximum age of log files in days.
     * Files older than this will be deleted.
     * Default: 90 days
     */
    var maxFileAgeDays: Int

    override fun startListening(scope: CoroutineScope): Job

    override fun stopListening()

    /**
     * Manually trigger cleanup of old log files.
     * This is called automatically on startup, but can also be triggered manually if needed.
     */
    fun performCleanup()
}