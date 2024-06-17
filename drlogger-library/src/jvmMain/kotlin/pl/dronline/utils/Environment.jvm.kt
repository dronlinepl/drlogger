package pl.dronline.utils

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Environment {
    actual fun get(key: String): String? = System.getenv(key)
}