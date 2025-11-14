plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.jfrogArtifactory).apply(false)
    alias(libs.plugins.dokka).apply(false)
}
