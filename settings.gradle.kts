pluginManagement {
    //includeBuild("convention-plugins")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            credentials {
                username = "guest"
                password = "MfG3nGpRZdPMSK"
            }
            url = uri("https://mvn.dr-online.pl/artifactory/plugin-dev-local")
        }
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
