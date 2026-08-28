/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * Abstract base implementation of ILogListener that provides common functionality for all log listeners.
 * This class handles the basic configuration aspects like enabled state, minimum log level, and message filtering.
 *
 * Concrete implementations need to extend this class and implement the writeLog method
 * to specify how log messages should be output to their respective destinations.
 *
 * @property name The unique identifier for this log listener
 */
abstract class ALogListener(override val name: String) : ILogListener {
    /**
     * The minimum log level this listener will process.
     * Messages with a severity level lower than this will be ignored.
     * Default to INFO level.
     */
    override var minLevel: ILogListener.Level = ILogListener.Level.INFO

    /**
     * Controls whether this log listener is active.
     * When set to false, the listener will not process any log messages.
     * Defaults to true.
     */
    override var enabled: Boolean = true

    /**
     * Optional regex pattern to filter log messages by content.
     * When set, only messages with content matching this pattern will be processed.
     * Default to null (no content filtering).
     */
    override var messageRegex: Regex? = null

    /**
     * Optional regex pattern to filter log messages by tag.
     * When set, only messages with tags matching this pattern will be processed.
     * Default to null (no tag filtering).
     */
    override var tagRegex: Regex? = null

    private var listeningJob: Job? = null

    /**
     * Determines if a log message should be processed by this listener.
     */
    protected fun shouldProcessMessage(message: LogMessage): Boolean {
        // Check if listener is enabled
        if (!enabled) return false

        // Early exit on level - most common filter and fastest check
        if (message.level < minLevel) return false

        // Apply tag filtering if configured
        tagRegex?.let { regex ->
            if (!regex.matches(message.type)) return false
        }

        // Apply message content filtering if configured
        messageRegex?.let { regex ->
            if (!regex.matches(message.data)) return false
        }

        return true
    }

    /**
     * Called when the listener starts listening.
     * Override this method to add custom startup behavior.
     */
    open fun onStart() {
        // Default implementation does nothing
    }

    /**
     * Starts listening to the log flow with this listener's specific processing logic.
     */
    @OptIn(ExperimentalTime::class)
    override fun startListening(scope: CoroutineScope): Job {
        // Cancel any existing job
        stopListening()

        // Call onStart callback
        onStart()

        val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            DrLoggerFactory.events.collect { event ->
                when (event) {
                    is LoggerEvent.Message -> {
                        val message = event.value
                        if (!shouldProcessMessage(message)) return@collect

                        try {
                            writeLog(
                                message.timestamp,
                                message.level,
                                message.type,
                                message.data,
                                message.throwable,
                            )
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (exception: Exception) {
                            consoleError("Error writing log in $name: ${exception.message}")
                        }
                    }

                    is LoggerEvent.Flush -> event.acknowledge()
                }
            }
        }
        listeningJob = job

        return job
    }

    /**
     * Stops listening to the log flow.
     */
    override fun stopListening() {
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun toString(): String {
        val messageRegexStr = messageRegex?.pattern?.let { ", messageRegex='$it'" }.orEmpty()
        val tagRegexStr = tagRegex?.pattern?.let { ", tagRegex='$it'" }.orEmpty()

        return "ALogListener(name='$name', minLevel=$minLevel$messageRegexStr$tagRegexStr)"
    }
}
