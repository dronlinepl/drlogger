package pl.dronline.utils.log

import kotlinx.datetime.Clock

/**
 * Represents a single log message with all associated metadata.
 * This class encapsulates all information needed to process and output a log entry.
 *
 * @param level The severity level of the log message
 * @param type The tag or category identifier for the log message
 * @param _data The raw log message content (will be sanitized)
 * @param throwable Optional throwable associated with this log message
 */
class LogMessage(
    val level: ILogListener.Level,
    val type: String,
    _data: String,
    val throwable: Throwable? = null
) {
    /** Timestamp when this log message was created */
    val timestamp = Clock.System.now()

    /** Sanitized log message data with null characters removed */
    val data = _data.replace("\u0000", "")

    override fun toString(): String {
        return "LogMessage(level=$level, type='$type', throwable=$throwable, timestamp=$timestamp, data='$data')"
    }
}