/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

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

    val listeners: List<ILogListener>
        get() = DrLoggerFactory.allListeners
}