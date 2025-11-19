/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

actual fun consolePrint(s: String) {
    print(s)
}

actual fun consoleError(s: String) {
    System.err.print(s)
}