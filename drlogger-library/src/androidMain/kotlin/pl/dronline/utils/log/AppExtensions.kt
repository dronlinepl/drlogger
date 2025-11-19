/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

import android.app.Application
import pl.dronline.utils.log.listener.DailyFileLogListener
import pl.dronline.utils.log.listener.LogcatLogListener
import kotlin.time.ExperimentalTime

/**
 * Enable logcat traces
 */
@ExperimentalTime
fun Application.enableLogcat() {
    DrLogger.addListener(LogcatLogListener())
}

/**
 * Enable traces YYYYMMDD.log
 * @path - where files will be stored
 */
@ExperimentalTime
fun Application.enableDailyFileLog(
    path: String
) {
    DrLogger.addListener(DailyFileLogListener(this))
}
