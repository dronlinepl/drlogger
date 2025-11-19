/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log.listener

import pl.dronline.utils.log.ALogListener
import pl.dronline.utils.log.ILogListener
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class LogcatLogListener : ALogListener("LogcatLogListener"), ILogListener {
    @ExperimentalTime
    actual override fun writeLog(timestamp: Instant, level: ILogListener.Level, type: String, message: String, t: Throwable?) {
        error("This platform does not support Logcat")
    }
    actual override var enabled: Boolean = false
}