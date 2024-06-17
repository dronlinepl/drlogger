package pl.dronline.utils.log.listener

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pl.dronline.utils.datetime.toString
import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.ILogListener
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.days

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DailyFileLogListener : ALogListener, ILogListener {

    private val context: Context
    private val lock = Object()
    private val _path = AtomicReference<String>()

    @Deprecated("Use DailyFileLogListener(context: Context)", level = DeprecationLevel.ERROR)
    actual constructor() : super("DailyFileLogListener") {
        this.context = Application()
    }

    constructor(context: Context) : super("DailyFileLogListener") {
        this.context = context
    }

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

    private val canBeUsed: Boolean
        get() {
            _path.get() ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return true
            } else {
                val neededPermissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )

                neededPermissions.forEach {
                    if (context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
                return true
            }
        }

    private val filename: Uri
        get() {
            return Uri.fromFile(File(path, "$namePrefix${Clock.System.now().toString("yyyyMMdd")}.log"))
        }

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
                    context.contentResolver.openFileDescriptor(filename, "wa", null)
                        ?.use { parcelFileDescriptor ->
                            FileOutputStream(parcelFileDescriptor.fileDescriptor).bufferedWriter()
                                .use {
                                    it.append(timestamp.toString("HH:mm:ss.SSS"))
                                    it.append(" [")
                                    it.append(level.name)
                                    it.append("] ")
                                    it.append(type)
                                    it.append("\t")
                                    it.append(message)
                                    t?.printStackTrace(PrintWriter(it))
                                    it.append("\n")
                                }
                        }
                }
            }
        }.onFailure {
            Log.wtf("DRONLINE", "Can not write log!", it)
        }
    }

    override fun onStart() {
        performCleanup()
    }

    actual fun performCleanup() {
        synchronized(lock) {
            val logPath = _path.get() ?: return

            // W Androidzie używamy ContentResolver z URI, ale do czyszczenia
            // możemy użyć File API jeśli mamy dostęp do katalogu
            runCatching {
                val logDir = File(logPath)
                if (!logDir.exists() || !logDir.isDirectory) {
                    Log.w("DailyFileLogListener", "Log directory does not exist or is not a directory: $logPath")
                    return
                }

                // Sprawdź czy mamy uprawnienia do czytania katalogu
                if (!logDir.canRead()) {
                    Log.w("DailyFileLogListener", "Cannot read log directory: $logPath")
                    return
                }

                val logFiles = logDir.listFiles { file ->
                    file.isFile &&
                            file.name.startsWith(namePrefix) &&
                            file.name.endsWith(".log")
                }?.sortedBy { it.lastModified() } ?: emptyList()

                if (logFiles.isEmpty()) {
                    Log.d("DailyFileLogListener", "No log files found for cleanup")
                    return
                }

                val now = Clock.System.now()
                val cutoffTime = now - maxFileAgeDays.days

                val filesToDelete = mutableListOf<File>()
                var remainingFiles = logFiles.toMutableList()

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
                Log.d("DailyFileLogListener", "Found ${filesToDelete.size} log files to delete")

                filesToDelete.forEach { file ->
                    runCatching {
                        if (file.delete()) {
                            Log.d("DailyFileLogListener", "Deleted old log file: ${file.name}")
                        } else {
                            Log.w("DailyFileLogListener", "Failed to delete log file: ${file.name}")
                        }
                    }.onFailure { e ->
                        Log.e("DailyFileLogListener", "Error deleting log file ${file.name}", e)
                    }
                }
            }.onFailure { e ->
                Log.e("DailyFileLogListener", "Error during log cleanup", e)
            }
        }
    }

    actual override var enabled: Boolean = true
}