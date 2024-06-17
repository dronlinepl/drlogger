pluginManagement {
    //includeBuild("convention-plugins")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "drlogger-library"
include(":drlogger-library")
