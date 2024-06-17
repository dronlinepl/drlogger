package pl.dronline.utils

import platform.Foundation.NSProcessInfo

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Environment {
    actual fun get(key: String): String? = NSProcessInfo.processInfo.environment[key] as? String
}