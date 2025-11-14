/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log.listener

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import pl.dronline.utils.datetime.toString
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.ILogListener
import platform.Foundation.*
import platform.posix.W_OK
import platform.posix.access
import kotlin.time.Clock
import kotlin.concurrent.AtomicReference
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@ExperimentalTime
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DailyFileLogListener : ALogListener("DailyFileLogListener"), ILogListener {
    private val lock = SynchronizedObject()
    private val _path = AtomicReference<String?>(null)

    actual var path: String?
        get() = _path.value
        set(value) {
            synchronized(lock) {
                _path.value = value
            }
        }

    actual var namePrefix: String = ""
    actual var maxFileCount: Int = 30
    actual var maxFileAgeDays: Int = 90

    private val canBeUsed: Boolean
        get() {
            return if (_path.value.isNullOrBlank()) {
                false
            } else {
                access(path, W_OK) == 0
            }
        }

    private val filename: String?
        get() {
            return _path.value?.let {
                "$it/$namePrefix${Clock.System.now().toString("yyyyMMdd")}.log"
            }
        }

    @OptIn(ExperimentalForeignApi::class)
    actual override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    ) {
        runCatching {
            synchronized(lock) {
                if (canBeUsed) {
                    val filePath = filename ?: return

                    // Najpierw sprawdzamy, czy plik istnieje i tworzymy go, jeśli nie
                    if (!NSFileManager.defaultManager.fileExistsAtPath(filePath)) {
                        NSFileManager.defaultManager.createFileAtPath(filePath, null, null)
                    }

                    // Teraz otwieramy plik do zapisu
                    val fileHandle = NSFileHandle.fileHandleForWritingAtPath(filePath)

                    // Jeśli udało się otworzyć plik, zapisujemy do niego
                    if (fileHandle != null) {
                        fileHandle.seekToEndOfFile()

                        val logMessage = buildString {
                            append(timestamp.toString("HH:mm:ss.SSS"))
                            append(" [")
                            append(level.name)
                            append("] ")
                            append(type)
                            append("\t")
                            append(message)
                            t?.let {
                                append("\n")
                                append(it.stackTraceToString())
                            }
                            append("\n")
                        }

                        fileHandle.writeData(logMessage.toNSData())
                        fileHandle.closeFile()
                    } else {
                        NSLog("Can not open file for write: $filePath")
                    }
                }
            }
        }.onFailure {
            NSLog("Can not write log! Error: $it")
        }
    }

    override fun onStart() {
        performCleanup()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun performCleanup() {
        synchronized(lock) {
            val logPath = _path.value ?: return

            runCatching {
                val fileManager = NSFileManager.defaultManager

                // Sprawdź czy katalog istnieje
                memScoped {
                    val isDirectory = alloc<BooleanVar>()
                    if (!fileManager.fileExistsAtPath(logPath, isDirectory.ptr) || !isDirectory.value) {
                        NSLog("Log directory does not exist or is not a directory: $logPath")
                        return
                    }
                }

                // Pobierz listę plików w katalogu
                val error = null
                val files = fileManager.contentsOfDirectoryAtPath(logPath, error) as? List<*>

                if (files == null || files.isEmpty()) {
                    NSLog("No files found in log directory")
                    return
                }

                // Filtruj tylko pliki logów
                val logFiles = mutableListOf<LogFileInfo>()

                files.forEach { file ->
                    val fileName = file as? String ?: return@forEach

                    if (fileName.startsWith(namePrefix) && fileName.endsWith(".log")) {
                        val filePath = "$logPath/$fileName"
                        val attributes = fileManager.attributesOfItemAtPath(filePath, null)

                        if (attributes != null) {
                            val modificationDate = attributes[NSFileModificationDate] as? NSDate
                            val fileSize = (attributes[NSFileSize] as? NSNumber)?.longValue ?: 0L

                            if (modificationDate != null) {
                                logFiles.add(LogFileInfo(
                                    path = filePath,
                                    name = fileName,
                                    size = fileSize,
                                    modifiedTime = (modificationDate.timeIntervalSince1970 * 1000).toLong()
                                ))
                            }
                        }
                    }
                }

                if (logFiles.isEmpty()) {
                    NSLog("No log files found for cleanup")
                    return
                }

                // Sortuj według czasu modyfikacji (najstarsze pierwsze)
                logFiles.sortBy { it.modifiedTime }

                val now = Clock.System.now()
                val cutoffTime = now - maxFileAgeDays.days

                val filesToDelete = mutableListOf<LogFileInfo>()
                var remainingFiles = logFiles.toMutableList()

                // Usuń pliki starsze niż maxFileAgeDays
                remainingFiles.forEach { file ->
                    val fileTime = Instant.fromEpochMilliseconds(file.modifiedTime)
                    if (fileTime < cutoffTime) {
                        filesToDelete.add(file)
                    }
                }
                remainingFiles.removeAll(filesToDelete)

                // Sprawdź limit liczby plików
                if (remainingFiles.size > maxFileCount) {
                    val countToDelete = remainingFiles.size - maxFileCount
                    filesToDelete.addAll(remainingFiles.take(countToDelete))
                }

                // Usuń zidentyfikowane pliki
                NSLog("Found ${filesToDelete.size} log files to delete")

                filesToDelete.forEach { file ->
                    val deleteError = null
                    if (fileManager.removeItemAtPath(file.path, deleteError)) {
                        NSLog("Deleted old log file: ${file.name}")
                    } else {
                        NSLog("Failed to delete log file ${file.name}: $deleteError")
                    }
                }
            }.onFailure { e ->
                NSLog("Error during log cleanup: ${e.message}")
            }
        }
    }

    private data class LogFileInfo(
        val path: String,
        val name: String,
        val size: Long,
        val modifiedTime: Long
    )

    actual override var enabled: Boolean = true
}

@Suppress("CAST_NEVER_SUCCEEDS")
private fun String.toNSData(): NSData {
    return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
}