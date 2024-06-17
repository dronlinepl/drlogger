package pl.dronline.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Environment {
    @OptIn(ExperimentalForeignApi::class)
    actual fun get(key: String): String? =
        getenv(key)?.toKString()
}