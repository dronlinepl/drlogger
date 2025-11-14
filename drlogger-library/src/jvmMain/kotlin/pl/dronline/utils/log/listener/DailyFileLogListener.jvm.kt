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
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ExperimentalTime
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DailyFileLogListener : ALogListener("DailyFileLogListener"), ILogListener {
    private val lock = Object()
    private val _path = AtomicReference<String>()

    actual var path: String?
        get() = _path.get()
        set(value) {
            synchronized(lock) {
                _path.set(value)
            }
        }

    actual var namePrefix: String = ""
    actual var maxFileCount: Int = 30
    actual var maxFileAgeDays: Int = 90

    @OptIn(ExperimentalTime::class)
    private val filename: File?
        get() {
            return _path.get()?.let {
                File(it, "$namePrefix${Clock.System.now().toString("yyyyMMdd")}.log")
            }
        }

    private val canBeUsed: Boolean
        get() {
            return filename?.let { file ->
                if (file.canWrite()) {
                    true
                } else {
                    file.parentFile?.let { parent ->
                        parent.isDirectory && parent.canWrite()
                    } ?: false
                }
            } ?: false
        }

    actual override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    ) {
        synchronized(lock) {
            runCatching {
                if (canBeUsed) {
                    val sb = StringBuilder()
                    sb.append(timestamp.toString("HH:mm:ss.SSS"))
                    sb.append(" [")
                    sb.append(level.name)
                    sb.append("] ")
                    sb.append(prepareMessage(type, message, t))

                    filename?.appendText(sb.toString())
                }
            }.onFailure {
                consoleError("CAN NOT WRITE: $level $type $message")
            }
        }
    }

    override fun onStart() {
        performCleanup()
    }

    actual fun performCleanup() {
        synchronized(lock) {
            val logDir = _path.get()?.let { File(it) } ?: return
            if (!logDir.exists() || !logDir.isDirectory) return

            runCatching {
                val logFiles = logDir.listFiles { file ->
                    file.isFile &&
                            file.name.startsWith(namePrefix) &&
                            file.name.endsWith(".log")
                }?.sortedBy { it.lastModified() } ?: return

                if (logFiles.isEmpty()) return

                val now = Clock.System.now()
                val cutoffTime = now - maxFileAgeDays.days

                // Delete files based on age
                val filesToDelete = mutableListOf<File>()
                val remainingFiles = logFiles.toMutableList()

                // Remove files older than maxFileAgeDays
                remainingFiles.forEach { file ->
                    val fileTime = Instant.fromEpochMilliseconds(file.lastModified())
                    if (fileTime < cutoffTime) {
                        filesToDelete.add(file)
                    }
                }
                remainingFiles.removeAll(filesToDelete)

                // Check file count limit
                if (remainingFiles.size > maxFileCount) {
                    val countToDelete = remainingFiles.size - maxFileCount
                    filesToDelete.addAll(remainingFiles.take(countToDelete))
                }

                // Delete identified files
                filesToDelete.forEach { file ->
                    runCatching {
                        if (file.delete()) {
                            consoleError("Deleted old log file: ${file.name}")
                        }
                    }.onFailure { e ->
                        consoleError("Failed to delete log file ${file.name}: ${e.message}")
                    }
                }
            }.onFailure { e ->
                consoleError("Error during log cleanup: ${e.message}")
            }
        }
    }

    actual override var enabled: Boolean = true
}