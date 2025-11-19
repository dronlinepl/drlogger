package pl.dronline.utils.log.listener

import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
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
        // Tworzymy tymczasowy katalog dla testów
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
        // Czyścimy po testach
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
    fun `should delete files older than maxFileAgeDays`() {
        // Given
        listener.maxFileAgeDays = 7 // Pliki starsze niż 7 dni

        // Tworzymy stare pliki
        val oldFile1 = createLogFile("test-20240101.log", 30) // 30 dni temu
        val oldFile2 = createLogFile("test-20240115.log", 15) // 15 dni temu
        val recentFile = createLogFile("test-20240201.log", 3) // 3 dni temu

        println("\nCreated test files:")
        println("  - ${oldFile1.name} (30 days old) exists: ${fileExists(oldFile1.path)}")
        println("  - ${oldFile2.name} (15 days old) exists: ${fileExists(oldFile2.path)}")
        println("  - ${recentFile.name} (3 days old) exists: ${fileExists(recentFile.path)}")

        // When
        println("\nRunning cleanup with maxFileAgeDays=${listener.maxFileAgeDays}...")
        listener.performCleanup()

        // Then
        println("\nAfter cleanup:")
        println("  - ${oldFile1.name} exists: ${fileExists(oldFile1.path)}")
        println("  - ${oldFile2.name} exists: ${fileExists(oldFile2.path)}")
        println("  - ${recentFile.name} exists: ${fileExists(recentFile.path)}")

        assertFalse(fileExists(oldFile1.path), "Old file 1 should be deleted")
        assertFalse(fileExists(oldFile2.path), "Old file 2 should be deleted")
        assertTrue(fileExists(recentFile.path), "Recent file should still exist")
    }

    @Test
    fun `should keep only maxFileCount files`() {
        // Given
        listener.maxFileCount = 3
        listener.maxFileAgeDays = 999 // Nie usuwamy ze względu na wiek

        println("\nTest: Keep only ${listener.maxFileCount} files")

        // Tworzymy 5 plików
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

        // Sprawdzamy że zostały 3 najnowsze pliki
        assertTrue(fileExists(files[2].path), "File 3 should exist")
        assertTrue(fileExists(files[3].path), "File 4 should exist")
        assertTrue(fileExists(files[4].path), "File 5 should exist")

        // A 2 najstarsze zostały usunięte
        assertFalse(fileExists(files[0].path), "File 1 should be deleted")
        assertFalse(fileExists(files[1].path), "File 2 should be deleted")
    }

    @Test
    fun `should handle empty directory`() {
        // Given - pusty katalog
        println("\n=== Test: Handle empty directory ===")

        // When & Then - nie powinno rzucić wyjątku
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

        // When & Then - nie powinno rzucić wyjątku
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

        listener.maxFileAgeDays = 5 // Wszystkie pliki są "stare"

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
        val oldFile = createLogFile("test-20240101.log", 10)

        println("\n=== Test: Cleanup on start ===")
        println("Created old file: ${oldFile.name}")

        // When
        listener.startListening(this)

        // Then - plik powinien zostać usunięty bo onStart wywołuje performCleanup
        println("After startListening: ${oldFile.name} exists: ${fileExists(oldFile.path)}")
        assertFalse(fileExists(oldFile.path), "Old file should be deleted on start")

        // Cleanup
        listener.stopListening()
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

    private fun createLogFile(name: String, daysAgo: Int): FileInfo {
        val filePath = "$tempDir/$name"
        val content = "Test log content for $name\n"
        writeFile(filePath, content)

        // Ustawiamy datę modyfikacji pliku
        val modifiedTime = Clock.System.now() - daysAgo.days
        val timeSpec = modifiedTime.epochSeconds

        memScoped {
            val times = allocArray<utimbuf>(1)
            times[0].actime = timeSpec
            times[0].modtime = timeSpec
            utime(filePath, times)
        }

        return FileInfo(filePath, name, content.length.toLong())
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