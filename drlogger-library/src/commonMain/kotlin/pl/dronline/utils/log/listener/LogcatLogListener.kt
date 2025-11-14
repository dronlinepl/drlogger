/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log.listener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import pl.dronline.utils.log.ILogListener
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class LogcatLogListener() : ILogListener {
    @OptIn(ExperimentalTime::class)
    override fun writeLog(
        timestamp: Instant,
        level: ILogListener.Level,
        type: String,
        message: String,
        t: Throwable?
    )

    override val name: String
    override var enabled: Boolean
    override var minLevel: ILogListener.Level
    override var messageRegex: Regex?
    override var tagRegex: Regex?
    override fun startListening(scope: CoroutineScope): Job
    override fun stopListening()

}
