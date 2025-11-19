/*
 * Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
 * Copyright (c) 2017-2025 Przemysław Dobrowolski
 *
 * SPDX-License-Identifier: MIT
 */

package pl.dronline.utils.log

import kotlin.reflect.KClass

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DrLogger actual constructor(tag: String) : DrLoggerCore(tag) {
    actual constructor(clazz: KClass<*>) : this(clazz.simpleName ?: clazz.toString())
    constructor(clazz: Class<*>) : this(clazz.simpleName ?: clazz.toString())

    actual companion object : DrLoggerImpl()
}
