package pl.dronline.utils

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object Environment  {
    fun get(key: String): String?
}