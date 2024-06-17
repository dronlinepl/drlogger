package pl.dronline.utils.log

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import pl.dronline.utils.datetime.toString

/**
 * Internal factory responsible for managing log listeners and dispatching log messages.
 * This factory provides centralized log management with optimized parallel processing
 * of log messages across multiple listeners.
 *
 * The factory uses coroutines to process log messages asynchronously, ensuring that
 * slow listeners (like file writers) don't block faster ones (like console loggers).
 */
internal object DrLoggerFactory {

    private var requiredLevel = atomic(ILogListener.Level.FATAL)

    internal fun prepareMessage(tag: String, message: String, t: Throwable?): String {
        val sb = StringBuilder()
        sb.append(tag)
        sb.append(" ")
        sb.append(message)

        if (t != null) {
            sb.append("\n")
            sb.append(t.stackTraceToString())
        }

        sb.append("\n")
        return sb.toString()
    }

    /**
     * Coroutine scope for listeners.
     * Each listener will launch its own coroutine in this scope.
     */
    private val listenerScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("DrLoggerFactory"))

    private val _listeners = mutableListOf<ILogListener>()

    val allListeners: List<ILogListener>
        get() = _listeners.toList()

    val listeners: List<ILogListener>
        get() = _listeners.toList().filter { it.enabled }

    /**
     * Shared flow for emitting log messages.
     * Listeners subscribe to this flow independently.
     * Using replay = 0 and extraBufferCapacity = 512 for better performance.
     */
    private val _events = MutableSharedFlow<LogMessage>(
        replay = 0,
        extraBufferCapacity = 512
    )

    val events = _events.asSharedFlow()

    val mutex = Mutex()
    val ignoredSources = ArrayList<String>()

    fun hasListeners(): Boolean = _listeners.isNotEmpty()

    fun getListenerByName(name: String): ILogListener? {
        return _listeners.firstOrNull { it.name == name }
    }

    /**
     * Adds listeners and starts their subscriptions.
     */
    fun addListeners(listeners: List<ILogListener>) {
        runBlocking {
            mutex.withLock {
                listeners.forEach { listener ->
                    _listeners.add(listener)
                    // Start the listener's subscription
                    listener.startListening(listenerScope)
                }
                recalculateRequiredLevel()
            }
        }
    }

    /**
     * Removes a listener and stops its subscription.
     */
    fun removeListener(listener: ILogListener) {
        runBlocking {
            mutex.withLock {
                if (_listeners.remove(listener)) {
                    listener.stopListening()
                    recalculateRequiredLevel()
                }
            }
        }
    }

    /**
     * Removes all listeners and stops their subscriptions.
     */
    fun clearListeners() {
        runBlocking {
            mutex.withLock {
                _listeners.forEach { it.stopListening() }
                _listeners.clear()
                requiredLevel.value = ILogListener.Level.FATAL
            }
        }
    }

    /**
     * Replaces all listeners with a single new listener.
     */
    fun setListener(listener: ILogListener) {
        runBlocking {
            mutex.withLock {
                // Stop all existing listeners
                _listeners.forEach { it.stopListening() }
                _listeners.clear()

                // Add and start the new listener
                _listeners.add(listener)

                requiredLevel.value = ILogListener.Level.FATAL
                onListenerAdded(listener)

                listener.startListening(listenerScope)
            }
        }
    }

    /**
     * Recalculate required log level based on all listeners
     * Use in case of changing log levels of existing listeners
     * */
    fun recalculateLogLevel() {
        runBlocking {
            mutex.withLock {
                recalculateRequiredLevel()
            }
        }
    }

    // Logging methods remain the same
    fun debug(type: String, message: String) {
        emitLog(LogMessage(ILogListener.Level.DEBUG, type, message))
    }

    fun trace(type: String, message: String) {
        emitLog(LogMessage(ILogListener.Level.TRACE, type, message))
    }

    fun info(type: String, message: String, t: Throwable? = null) {
        emitLog(LogMessage(ILogListener.Level.INFO, type, message, t))
    }

    fun warn(type: String, message: String, t: Throwable? = null) {
        emitLog(LogMessage(ILogListener.Level.WARN, type, message, t))
    }

    fun error(type: String, message: String, t: Throwable? = null) {
        emitLog(LogMessage(ILogListener.Level.ERROR, type, message, t))
    }

    fun fatal(type: String, message: String, t: Throwable? = null) {
        emitLog(LogMessage(ILogListener.Level.FATAL, type, message, t))
    }

    /**
     * Emits a log message to the shared flow.
     * All subscribed listeners will receive this message.
     */
    private fun emitLog(logMessage: LogMessage) {
        // Check ignored sources
        if (logMessage.type in ignoredSources) return

        // Do not emit if level too low
        if (logMessage.level < requiredLevel.value) return

        // tryEmit is non-blocking and returns false if buffer is full
        if (!_events.tryEmit(logMessage)) {
            // Log dropped message in debug mode
            val timestamp = Clock.System.now().toString("HH:mm:ss.SSS")
            println("[${timestamp}] Warning: Log message delayed due to buffer overflow: ${logMessage.type}")
            listenerScope.launch {
                _events.emit(logMessage)
            }

        }
    }

    private fun onListenerAdded(listener: ILogListener) {
        if (listener.enabled) {
            val listenerLevel = listener.minLevel
            if (listenerLevel < requiredLevel.value) {
                requiredLevel.value = listenerLevel
            }
        }
    }

    private fun recalculateRequiredLevel() {
        val activeListeners = _listeners.filter { it.enabled }
        if (activeListeners.isEmpty()) {
            requiredLevel.value = ILogListener.Level.FATAL
        } else {
            var minLevel = ILogListener.Level.FATAL
            activeListeners.forEach { listener ->
                if (listener.minLevel < minLevel) {
                    minLevel = listener.minLevel
                }
            }
            requiredLevel.value = minLevel
        }
    }


}
