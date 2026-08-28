/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.reflect.KClass


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DrLogger(tag: String) : DrLoggerCore {

    constructor(clazz: KClass<*>)

    companion object : DrLoggerImpl
}

open class DrLoggerImpl {

    fun getListenerByName(name: String): ILogListener? {
        return DrLoggerFactory.getListenerByName(name)
    }

    fun addListener(vararg listeners: ILogListener) {
        DrLoggerFactory.addListeners(listeners.toList())
    }

    fun setListener(listener: ILogListener) {
        DrLoggerFactory.setListener(listener)
    }

    /**
     * Recalculate required log level based on all listeners
     * Use in case of changing log levels of existing listeners
     * */
    fun recalculateLogLevel() {
        DrLoggerFactory.recalculateLogLevel()
    }

    /**
     * Waits until every listener has processed all log messages emitted before this call.
     * Returns false when the timeout expires.
     */
    suspend fun flush(timeout: Duration = 5.seconds): Boolean {
        return DrLoggerFactory.flush(timeout)
    }

    /**
     * Blocking variant of [flush] for non-coroutine entry points.
     */
    fun flushBlocking(timeout: Duration = 5.seconds): Boolean {
        return DrLoggerFactory.flushBlocking(timeout)
    }

    /**
     * Flushes pending messages and stops all listeners.
     * Returns false when flushing timed out; listeners are stopped in either case.
     */
    suspend fun shutdown(timeout: Duration = 5.seconds): Boolean {
        return DrLoggerFactory.shutdown(timeout)
    }

    /**
     * Blocking variant of [shutdown] for non-coroutine entry points.
     */
    fun shutdownBlocking(timeout: Duration = 5.seconds): Boolean {
        return DrLoggerFactory.shutdownBlocking(timeout)
    }

    val listeners: List<ILogListener>
        get() = DrLoggerFactory.allListeners
}
