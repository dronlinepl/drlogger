/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log.listener

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import pl.dronline.utils.datetime.toString
import pl.dronline.utils.filesystem.FileSize
import pl.dronline.utils.filesystem.FileSystem
import pl.dronline.utils.filesystem.mkdirs
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.DrLoggerFactory.prepareMessage
import pl.dronline.utils.log.ILogListener
import pl.dronline.utils.log.consoleError
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Base implementation of daily file log listener.
 * Use [DailyFileLogListener] for the public API.
 */
@OptIn(ExperimentalTime::class)
open class BaseDailyFileLogListener : ALogListener("DailyFileLogListener"), ILogListener {
    private data class LogFile(
        val name: String,
        val date: String,
        val rotationIndex: Int,
    )

    private val _path = atomic<String?>(null)
    private val _rotationFailedForFile = atomic<String?>(null)

    var path: String?
        get() = _path.value
        set(value) {
            value?.let { mkdirs(it) }
            _path.value = value
        }

    var namePrefix: String = ""
    var maxFileCount: Int = 30
    var maxFileAgeDays: Int = 90
    var maxFileSize: FileSize? = null

    private val filename: String?
        get() {
            val basePath = _path.value ?: return null
            val dateStr = Clock.System.now().toString("yyyyMMdd")
            val baseFilename = "$namePrefix$dateStr"

            val mainFileName = "$baseFilename.log"
            val mainFilePath = FileSystem.joinPath(basePath, mainFileName)

            val maxSize = maxFileSize?.bytes
            if (maxSize == null) {
                return mainFilePath
            }

            // Rotation already failed for this file today - don't try again
            if (_rotationFailedForFile.value == baseFilename) {
                return mainFilePath
            }

            val mainSize = FileSystem.getFileSize(mainFilePath)
            if (mainSize < 0 || mainSize < maxSize) {
                return mainFilePath
            }

            // Nginx-style rotation: rotate existing files and always write to main file
            if (!rotateFiles(basePath, baseFilename)) {
                _rotationFailedForFile.value = baseFilename
                writeLog(
                    timestamp = Clock.System.now(),
                    level = ILogListener.Level.WARN,
                    type = name,
                    message = "File rotation failed for $baseFilename, size limit will be exceeded",
                    t = null
                )
            }
            return mainFilePath
        }

    /**
     * Nginx-style rotation:
     * - app.log → app.1.log
     * - app.1.log → app.2.log
     * - app.2.log → app.3.log
     * - etc.
     *
     * @return true if rotation succeeded, false if main file rename failed
     */
    private fun rotateFiles(basePath: String, baseFilename: String): Boolean {
        // Find highest existing index
        var highestIndex = 0
        while (true) {
            val nextIndex = highestIndex + 1
            val indexedPath = FileSystem.joinPath(basePath, "$baseFilename.$nextIndex.log")
            if (FileSystem.getFileSize(indexedPath) < 0) {
                break
            }
            highestIndex = nextIndex
        }

        // Rotate from highest to lowest: .N → .N+1, ..., .1 → .2
        for (i in highestIndex downTo 1) {
            val fromPath = FileSystem.joinPath(basePath, "$baseFilename.$i.log")
            val toPath = FileSystem.joinPath(basePath, "$baseFilename.${i + 1}.log")
            FileSystem.renameFile(fromPath, toPath)
        }

        // Rotate main file: app.log → app.1.log
        val mainPath = FileSystem.joinPath(basePath, "$baseFilename.log")
        val firstIndexPath = FileSystem.joinPath(basePath, "$baseFilename.1.log")
        return FileSystem.renameFile(mainPath, firstIndexPath)
    }

    private val canBeUsed: Boolean
        get() {
            val dir = _path.value ?: return false
            return FileSystem.canWrite(dir)
        }

    override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    ) {
        runCatching {
            if (canBeUsed) {
                val logMessage = buildString {
                    append(timestamp.toString("HH:mm:ss.SSS"))
                    append(" [")
                    append(level.name)
                    append("] ")
                    append(prepareMessage(type, message, t))
                }

                filename?.let { path ->
                    FileSystem.appendToFile(path, logMessage)
                }
            }
        }.onFailure {
            consoleError("CAN NOT WRITE: $level $type $message")
        }
    }

    override fun onStart() {
        performCleanup()
    }

    fun performCleanup() {
        val logDirPath = _path.value ?: return

        runCatching {
            val pattern = Regex("^${Regex.escape(namePrefix)}(\\d{8})(?:\\.(\\d+))?\\.log$")
            val logFiles = FileSystem.listFiles(logDirPath)
                .mapNotNull { file ->
                    val match = pattern.matchEntire(file.name) ?: return@mapNotNull null
                    LogFile(
                        name = file.name,
                        date = match.groupValues[1],
                        rotationIndex = match.groupValues[2].toIntOrNull() ?: 0,
                    )
                }
                .sortedWith(compareBy<LogFile> { it.date }.thenByDescending { it.rotationIndex })

            if (logFiles.isEmpty()) return

            val cutoffDate = (Clock.System.now() - maxFileAgeDays.days).toString("yyyyMMdd")

            val filesToDelete = mutableListOf<String>()
            val remainingFiles = logFiles.toMutableList()

            remainingFiles.forEach { file ->
                if (file.date < cutoffDate) {
                    filesToDelete.add(file.name)
                }
            }
            remainingFiles.removeAll { filesToDelete.contains(it.name) }

            if (remainingFiles.size > maxFileCount) {
                val countToDelete = remainingFiles.size - maxFileCount
                filesToDelete.addAll(remainingFiles.take(countToDelete).map { it.name })
            }

            filesToDelete.forEach { fileName ->
                val filePath = FileSystem.joinPath(logDirPath, fileName)
                runCatching {
                    if (FileSystem.deleteFile(filePath)) {
                        consoleError("Deleted old log file: $fileName")
                    }
                }.onFailure { e ->
                    consoleError("Failed to delete log file $fileName: ${e.message}")
                }
            }
        }.onFailure { e ->
            consoleError("Error during log cleanup: ${e.message}")
        }
    }
}

/**
 * A log listener implementation that writes log messages to daily rotating log files.
 *
 * Features:
 * - Daily log rotation
 * - Automatic cleanup of old log files on startup
 * - Maximum file count limit
 * - Maximum file age limit
 * - Optional size-based rolling within a day (nginx-style)
 *
 * When size-based rolling is enabled (maxFileSize is not null), files use nginx-style rotation:
 * - `prefix20250123.log` - current file (always newest)
 * - `prefix20250123.1.log` - previous file
 * - `prefix20250123.2.log` - older file
 * - etc. (higher index = older file)
 */
@OptIn(ExperimentalTime::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DailyFileLogListener() : ILogListener {
    override val name: String
    override var enabled: Boolean
    override var minLevel: ILogListener.Level
    override var messageRegex: Regex?
    override var tagRegex: Regex?

    var path: String?
    var namePrefix: String
    var maxFileCount: Int
    var maxFileAgeDays: Int
    var maxFileSize: FileSize?

    override fun writeLog(timestamp: Instant, level: ILogListener.Level, type: String, message: String, t: Throwable?)
    override fun startListening(scope: CoroutineScope): Job
    override fun stopListening()
    fun performCleanup()
}
