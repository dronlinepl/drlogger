package pl.dronline.utils.log.listener

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import pl.dronline.utils.datetime.toString
import pl.dronline.utils.log.ILogListener
import platform.posix.*
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@ExperimentalTime
@OptIn(ExperimentalForeignApi::class)
class DailyFileLogListenerTest {

    private lateinit var tempDir: String
    private lateinit var listener: DailyFileLogListener

    @BeforeTest
    fun setup() {
        // Create a temporary directory for tests
        val template = "/tmp/log-test-XXXXXX"
        val buffer = ByteArray(template.length + 1)
        template.encodeToByteArray().copyInto(buffer)

        val dirPath = mkdtemp(buffer.refTo(0))
        assertNotNull(dirPath, "Failed to create temp directory")

        tempDir = dirPath.toKString()
        println("\n=== Test Setup ===")
        println("Created temp directory: $tempDir")

        listener = DailyFileLogListener().apply {
            path = tempDir
            namePrefix = "test-"
            enabled = true
        }
    }

    @AfterTest
    fun tearDown() {
        // Clean up after tests
        println("\n=== Test Cleanup ===")
        println("Deleting temp directory: $tempDir")
        deleteDirectory(tempDir)
    }

    @Test
    fun `should create log file when writing`() {
        // Given
        val testMessage = "Test log message"
        println("\n=== Test: Create log file ===")

        // When
        println("Writing log message: '$testMessage'")
        listener.writeLog(
            timestamp = Clock.System.now(),
            level = ILogListener.Level.INFO,
            type = "TEST",
            message = testMessage,
            t = null
        )

        // Then
        val logFiles = listLogFiles()

        println("Found log files: ${logFiles.size}")
        logFiles.forEach { file ->
            println("  - ${file.name} (${file.size} bytes)")
            val content = readFile(file.path)
            println("    Content preview: ${content.take(100).trim()}")
        }

        assertEquals(1, logFiles.size)
        assertTrue(readFile(logFiles[0].path).contains(testMessage))
    }

    @Test
    fun `should determine file age from filename instead of modification time`() {
        // Given
        listener.maxFileAgeDays = 7

        val oldFile = createLogFile(logFileName(daysAgo = 30), modifiedDaysAgo = 0)
        val recentFile = createLogFile(logFileName(daysAgo = 3), modifiedDaysAgo = 30)

        // When
        listener.performCleanup()

        // Then
        assertFalse(fileExists(oldFile.path), "Filename date should cause the old file to be deleted")
        assertTrue(fileExists(recentFile.path), "Recent file should still exist")
    }

    @Test
    fun `should keep only maxFileCount files`() {
        // Given
        listener.maxFileCount = 3
        listener.maxFileAgeDays = 9999 // Do not delete based on age

        println("\nTest: Keep only ${listener.maxFileCount} files")

        // Create 5 files
        val files = (1..5).map { i ->
            createLogFile("test-2024010$i.log", 5 - i)
        }

        println("\nCreated ${files.size} test files:")
        files.forEachIndexed { index, file ->
            println("  - ${file.name} (${5 - index} days old)")
        }

        // When
        println("\nRunning cleanup...")
        listener.performCleanup()

        // Then
        val remainingFiles = listLogFiles()

        println("\nAfter cleanup - remaining files: ${remainingFiles.size}")
        remainingFiles.forEach { file ->
            println("  - ${file.name}")
        }

        assertEquals(3, remainingFiles.size)

        // Verify that the 3 newest files remain
        assertTrue(fileExists(files[2].path), "File 3 should exist")
        assertTrue(fileExists(files[3].path), "File 4 should exist")
        assertTrue(fileExists(files[4].path), "File 5 should exist")

        // Verify that the 2 oldest files were deleted
        assertFalse(fileExists(files[0].path), "File 1 should be deleted")
        assertFalse(fileExists(files[1].path), "File 2 should be deleted")
    }

    @Test
    fun `should handle empty directory`() {
        // Given - empty directory
        println("\n=== Test: Handle empty directory ===")

        // When & Then - should not throw
        assertDoesNotThrow {
            listener.performCleanup()
        }
        println("Cleanup completed without errors")
    }

    @Test
    fun `should handle non-existent directory`() {
        // Given
        listener.path = "/non/existent/path"
        println("\n=== Test: Handle non-existent directory ===")
        println("Testing with path: ${listener.path}")

        // When & Then - should not throw
        assertDoesNotThrow {
            listener.performCleanup()
        }
        println("Cleanup completed without errors")
    }

    @Test
    fun `should ignore non-log files`() {
        // Given
        val logFile = createLogFile("test-20240101.log", 10)
        val otherFile = createFile("other-file.txt", "Not a log file")
        val wrongPrefixFile = createFile("wrong-20240101.log", "Wrong prefix")

        listener.maxFileAgeDays = 5 // All matching log files are old

        println("\n=== Test: Ignore non-log files ===")
        println("Created files:")
        println("  - ${logFile.name} (should be deleted)")
        println("  - ${otherFile.name} (should be kept)")
        println("  - ${wrongPrefixFile.name} (should be kept)")

        // When
        listener.performCleanup()

        // Then
        println("\nAfter cleanup:")
        println("  - ${logFile.name} exists: ${fileExists(logFile.path)}")
        println("  - ${otherFile.name} exists: ${fileExists(otherFile.path)}")
        println("  - ${wrongPrefixFile.name} exists: ${fileExists(wrongPrefixFile.path)}")

        assertFalse(fileExists(logFile.path), "Log file should be deleted")
        assertTrue(fileExists(otherFile.path), "Non-log file should not be deleted")
        assertTrue(fileExists(wrongPrefixFile.path), "File with wrong prefix should not be deleted")
    }

    @Test
    fun `cleanup should be called on start`() = runBlocking {
        // Given
        listener.maxFileAgeDays = 1
        val oldFile = createLogFile(logFileName(daysAgo = 10), modifiedDaysAgo = 0)

        println("\n=== Test: Cleanup on start ===")
        println("Created old file: ${oldFile.name}")

        // When
        listener.startListening(this)

        // Then - the file should be deleted because onStart calls performCleanup
        println("After startListening: ${oldFile.name} exists: ${fileExists(oldFile.path)}")
        assertFalse(fileExists(oldFile.path), "Old file should be deleted on start")

        // Cleanup
        listener.stopListening()
    }

    @Test
    fun `should keep newest rotation files regardless of modification time`() {
        // Given
        listener.maxFileCount = 2
        listener.maxFileAgeDays = 9999
        val date = Clock.System.now().toString("yyyyMMdd")
        val mainFile = createLogFile("test-$date.log", modifiedDaysAgo = 30)
        val previousFile = createLogFile("test-$date.1.log", modifiedDaysAgo = 20)
        val oldestFile = createLogFile("test-$date.2.log", modifiedDaysAgo = 0)

        // When
        listener.performCleanup()

        // Then
        assertTrue(fileExists(mainFile.path), "Main file should be treated as the newest rotation file")
        assertTrue(fileExists(previousFile.path), "First indexed file should be retained")
        assertFalse(fileExists(oldestFile.path), "Highest rotation index should be deleted first")
    }

    // Helper functions

    private data class FileInfo(
        val path: String,
        val name: String,
        val size: Long
    )

    private fun listLogFiles(): List<FileInfo> {
        val files = mutableListOf<FileInfo>()
        val dir = opendir(tempDir) ?: return files

        try {
            while (true) {
                val entry = readdir(dir) ?: break
                val fileName = entry.pointed.d_name.toKString()

                if (fileName.startsWith(listener.namePrefix) && fileName.endsWith(".log")) {
                    val filePath = "$tempDir/$fileName"
                    memScoped {
                        val statBuf = alloc<stat>()
                        if (stat(filePath, statBuf.ptr) == 0) {
                            files.add(
                                FileInfo(
                                    path = filePath,
                                    name = fileName,
                                    size = statBuf.st_size
                                )
                            )
                        }
                    }
                }
            }
        } finally {
            closedir(dir)
        }

        return files
    }

    private fun createLogFile(name: String, modifiedDaysAgo: Int): FileInfo {
        val filePath = "$tempDir/$name"
        val content = "Test log content for $name\n"
        writeFile(filePath, content)

        // Set the file modification time
        val modifiedTime = Clock.System.now() - modifiedDaysAgo.days
        val timeSpec = modifiedTime.epochSeconds

        memScoped {
            val times = allocArray<utimbuf>(1)
            times[0].actime = timeSpec
            times[0].modtime = timeSpec
            utime(filePath, times)
        }

        return FileInfo(filePath, name, content.length.toLong())
    }

    private fun logFileName(daysAgo: Int): String {
        val date = (Clock.System.now() - daysAgo.days).toString("yyyyMMdd")
        return "test-$date.log"
    }

    private fun createFile(name: String, content: String): FileInfo {
        val filePath = "$tempDir/$name"
        writeFile(filePath, content)
        return FileInfo(filePath, name, content.length.toLong())
    }

    private fun writeFile(path: String, content: String) {
        val file = fopen(path, "w")
        assertNotNull(file, "Failed to create file: $path")
        fputs(content, file)
        fclose(file)
    }

    private fun readFile(path: String): String {
        val file = fopen(path, "r") ?: return ""
        val content = buildString {
            val buffer = ByteArray(1024)
            buffer.usePinned { pinned ->
                while (true) {
                    val bytesRead = fread(pinned.addressOf(0), 1u, buffer.size.toULong(), file)
                    if (bytesRead == 0UL) break
                    append(buffer.decodeToString(0, bytesRead.toInt()))
                }
            }
        }
        fclose(file)
        return content
    }

    private fun fileExists(path: String): Boolean {
        return access(path, F_OK) == 0
    }

    private fun deleteDirectory(path: String) {
        val dir = opendir(path) ?: return

        try {
            while (true) {
                val entry = readdir(dir) ?: break
                val name = entry.pointed.d_name.toKString()

                if (name != "." && name != "..") {
                    val fullPath = "$path/$name"
                    remove(fullPath)
                }
            }
        } finally {
            closedir(dir)
        }

        rmdir(path)
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Expected no exception but got: ${e.message}")
        }
    }
}
