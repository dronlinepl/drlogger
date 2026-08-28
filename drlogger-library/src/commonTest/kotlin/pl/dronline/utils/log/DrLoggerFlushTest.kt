package pl.dronline.utils.log

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class DrLoggerFlushTest {
    @BeforeTest
    fun setUp() {
        DrLoggerFactory.clearListeners()
        assertTrue(DrLogger.flushBlocking(5.seconds))
    }

    private class RecordingListener : ALogListener("RecordingListener") {
        val messages = mutableListOf<String>()

        @OptIn(ExperimentalTime::class)
        override fun writeLog(
            timestamp: Instant,
            level: ILogListener.Level,
            type: String,
            message: String,
            t: Throwable?,
        ) {
            messages.add(message)
        }
    }

    @AfterTest
    fun tearDown() {
        DrLogger.shutdownBlocking()
    }

    @Test
    fun addListener_immediateLog_flushProcessesMessage() {
        val listener = RecordingListener()
        val disabledListener = RecordingListener().apply { enabled = false }

        DrLogger.addListener(listener, disabledListener)
        DrLogger("TEST").info("immediate message")

        assertTrue(DrLogger.flushBlocking(5.seconds))
        assertEquals(listOf("immediate message"), listener.messages)
        assertTrue(disabledListener.messages.isEmpty())
    }

    @Test
    fun shutdownBlocking_pendingMessage_flushesAndClearsListeners() {
        val listener = RecordingListener()

        DrLogger.addListener(listener)
        DrLogger("TEST").info("final message")

        assertTrue(DrLogger.shutdownBlocking(5.seconds))
        assertEquals(listOf("final message"), listener.messages)
        assertTrue(DrLogger.listeners.isEmpty())
    }

    @Test
    fun addListener_sameInstanceTwice_flushWaitsForSingleCollector() {
        val listener = RecordingListener()

        DrLogger.addListener(listener)
        DrLogger.addListener(listener)
        DrLogger("TEST").info("deduplicated message")

        assertTrue(DrLogger.flushBlocking(5.seconds))
        assertEquals(1, DrLogger.listeners.size)
        assertEquals(listOf("deduplicated message"), listener.messages)
    }

    @Test
    fun logging_saturatedBuffer_doesNotBlockProducer() = runBlocking {
        val listenerStarted = atomic(false)
        val releaseListener = atomic(false)
        val processedMessages = atomic(0)
        val blockingListener = object : ALogListener("BlockingListener") {
            @OptIn(ExperimentalTime::class)
            override fun writeLog(
                timestamp: Instant,
                level: ILogListener.Level,
                type: String,
                message: String,
                t: Throwable?,
            ) {
                processedMessages.incrementAndGet()
                if (message == "block") {
                    listenerStarted.value = true
                    while (!releaseListener.value) {
                        // Keep the collector occupied while the producer saturates the old buffer limit.
                    }
                }
            }
        }

        DrLogger.addListener(blockingListener)
        DrLogger("TEST").info("block")
        withTimeout(5.seconds) {
            while (!listenerStarted.value) delay(1)
        }

        val producer = launch(Dispatchers.Default) {
            repeat(1_200) { index ->
                DrLogger("TEST").info("message $index")
            }
        }

        try {
            withTimeout(5.seconds) {
                producer.join()
            }
        } finally {
            releaseListener.value = true
        }

        assertTrue(DrLogger.flush(5.seconds))
        assertTrue(processedMessages.value < 1_201, "A saturated queue should drop messages instead of growing without bounds")
    }
}
