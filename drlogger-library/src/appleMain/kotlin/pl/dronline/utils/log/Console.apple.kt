/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr

actual fun consolePrint(s: String) {
    print(s)
}

@OptIn(ExperimentalForeignApi::class)
actual fun consoleError(s: String) {
    fprintf(stderr, s)
}