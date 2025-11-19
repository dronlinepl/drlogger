package pl.dronline.utils.log.listener

import kotlinx.coroutines.runBlocking
import pl.dronline.utils.log.ILogListener
import java.io.File
import java.nio.file.Files
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DailyFileLogListenerTest {

    private lateinit var tempDir: File
    private lateinit var listener: DailyFileLogListener

    @BeforeTest
    fun setup() {
        // Tworzymy tymczasowy katalog dla testów
        tempDir = Files.createTempDirectory("log-test-").toFile()
        println("\n=== Test Setup ===")
        println("Created temp directory: ${tempDir.absolutePath}")

        listener = DailyFileLogListener().apply {
            path = tempDir.absolutePath
            namePrefix = "test-"
            enabled = true
        }
    }

    @AfterTest
    fun tearDown() {
        // Czyścimy po testach
        println("\n=== Test Cleanup ===")
        println("Deleting temp directory: ${tempDir.absolutePath}")
        tempDir.deleteRecursively()
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
        val logFiles = tempDir.listFiles { file ->
            file.name.startsWith("test-") && file.name.endsWith(".log")
        }

        println("Found log files: ${logFiles?.size ?: 0}")
        logFiles?.forEach { file ->
            println("  - ${file.name} (${file.length()} bytes)")
            println("    Content preview: ${file.readText().take(100).trim()}")
        }

        assertNotNull(logFiles)
        assertEquals(1, logFiles.size)
        assertTrue(logFiles[0].readText().contains(testMessage))
    }

    @Test
    fun `should delete files older than maxFileAgeDays`() {
        // Given
        listener.maxFileAgeDays = 7 // Pliki starsze niż 7 dni

        // Tworzymy stare pliki
        val oldFile1 = createLogFile("test-20240101.log", 30) // 30 dni temu
        val oldFile2 = createLogFile("test-20240115.log", 15) // 15 dni temu
        val recentFile = createLogFile("test-20240201.log", 3) // 3 dni temu

        println("Created test files:")
        println("  - ${oldFile1.name} (30 days old) exists: ${oldFile1.exists()}")
        println("  - ${oldFile2.name} (15 days old) exists: ${oldFile2.exists()}")
        println("  - ${recentFile.name} (3 days old) exists: ${recentFile.exists()}")

        // When
        println("\nRunning cleanup with maxFileAgeDays=${listener.maxFileAgeDays}...")
        listener.performCleanup()

        // Then
        println("\nAfter cleanup:")
        println("  - ${oldFile1.name} exists: ${oldFile1.exists()}")
        println("  - ${oldFile2.name} exists: ${oldFile2.exists()}")
        println("  - ${recentFile.name} exists: ${recentFile.exists()}")

        assertFalse(oldFile1.exists(), "Old file 1 should be deleted")
        assertFalse(oldFile2.exists(), "Old file 2 should be deleted")
        assertTrue(recentFile.exists(), "Recent file should still exist")
    }

    @Test
    fun `should keep only maxFileCount files`() {
        // Given
        listener.maxFileCount = 3
        listener.maxFileAgeDays = 999 // Nie usuwamy ze względu na wiek

        println("Test: Keep only ${listener.maxFileCount} files")

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
        val remainingFiles = tempDir.listFiles { file ->
            file.name.startsWith("test-") && file.name.endsWith(".log")
        }

        println("\nAfter cleanup - remaining files: ${remainingFiles?.size ?: 0}")
        remainingFiles?.forEach { file ->
            println("  - ${file.name}")
        }

        assertEquals(3, remainingFiles?.size ?: 0)

        // Sprawdzamy że zostały 3 najnowsze pliki
        assertTrue(files[2].exists(), "File 3 should exist")
        assertTrue(files[3].exists(), "File 4 should exist")
        assertTrue(files[4].exists(), "File 5 should exist")

        // A 2 najstarsze zostały usunięte
        assertFalse(files[0].exists(), "File 1 should be deleted")
        assertFalse(files[1].exists(), "File 2 should be deleted")
    }

    @Test
    fun `should handle empty directory`() {
        // Given - pusty katalog

        // When & Then - nie powinno rzucić wyjątku
        assertDoesNotThrow {
            listener.performCleanup()
        }
    }

    @Test
    fun `should handle non-existent directory`() {
        // Given
        listener.path = "/non/existent/path"

        // When & Then - nie powinno rzucić wyjątku
        assertDoesNotThrow {
            listener.performCleanup()
        }
    }

    @Test
    fun `should ignore non-log files`() {
        // Given
        val logFile = createLogFile("test-20240101.log", 10)
        val otherFile = File(tempDir, "other-file.txt").apply {
            writeText("Not a log file")
        }
        val wrongPrefixFile = File(tempDir, "wrong-20240101.log").apply {
            writeText("Wrong prefix")
        }

        listener.maxFileAgeDays = 5 // Wszystkie pliki są "stare"

        // When
        listener.performCleanup()

        // Then
        assertFalse(logFile.exists(), "Log file should be deleted")
        assertTrue(otherFile.exists(), "Non-log file should not be deleted")
        assertTrue(wrongPrefixFile.exists(), "File with wrong prefix should not be deleted")
    }

    @Test
    fun `cleanup should be called on start`() = runBlocking {
        // Given
        listener.maxFileAgeDays = 1
        val oldFile = createLogFile("test-20240101.log", 10)

        // When
        listener.startListening(this)

        // Then - plik powinien zostać usunięty bo onStart wywołuje performCleanup
        assertFalse(oldFile.exists(), "Old file should be deleted on start")

        // Cleanup
        listener.stopListening()
    }

    // Helper functions

    private fun createLogFile(name: String, daysAgo: Int): File {
        val file = File(tempDir, name)
        file.writeText("Test log content\n")

        // Ustawiamy datę modyfikacji pliku
        val modifiedTime = Clock.System.now() - daysAgo.days
        file.setLastModified(modifiedTime.toEpochMilliseconds())

        return file
    }

    private fun assertDoesNotThrow(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            fail("Expected no exception but got: ${e.message}")
        }
    }
}