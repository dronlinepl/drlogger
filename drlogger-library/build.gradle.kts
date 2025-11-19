plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.jfrogArtifactory)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
    `maven-publish`
}

val longVersion = "1.0.${gitRevisionStrNr}"
version = longVersion


kotlin {
    jvm()

    androidTarget {
        publishLibraryVariants("release")
    }

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    // iOS and macOS targets can only be built on macOS
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
        macosArm64()
        macosX64()
    }

    // Linux targets
    linuxX64()
    linuxArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinxSerializationJson)
                implementation(libs.kotlin.coroutines)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.atomicfu)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.log4j.core)
            }
        }
    }
}

android {
    namespace = "pl.dronline.multiplatform.utils"
    group
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}


artifactory {
    publish {
        defaults {
            publications(
                "kotlinMultiplatform",
                "androidRelease",
                "iosX64",
                "iosArm64",
                "iosSimulatorArm64",
                "jvm",
                "android",
                "macosArm64",
                "macosX64",
                "linuxX64",
                "linuxArm64"
            )
            setPublishArtifacts(true)
        }
    }
}


tasks.register("printVersion") {
    doLast {
        println(version)
    }
}

dokka {
    dokkaPublications.html {
        suppressInheritedMembers.set(true)
        failOnWarning.set(true)
    }

    pluginsConfiguration.html {
        footerMessage.set("(c) 2017-2025 DR-ONLINE SP. Z O.O.")
    }
}

val gitRevisionStrNr: String
    get() {
        val result: ExecOutput = providers.exec {
            if (!org.gradle.internal.os.OperatingSystem.current().isWindows()) {
                commandLine = listOf("/bin/bash", "-c", "git log --oneline | wc -l")
            } else {
                commandLine =
                    listOf("powershell", "-c", "(git log --oneline | Measure-Object -line).Lines")
            }
        }
        val ret = result.standardOutput.asText.get().trim()
        println("ret=$ret")
        return ret
    }


mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates("pl.dronline.multiplatform.utils", "drlogger", version.toString())

    pom {
        name = "Drlogger Library"
        description = "Logging library based on log4j for Kotlin Multiplatform projects"
        inceptionYear = "2025"
        url = "https://github.com/dronlinepl/drlogger"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "dronlinepl"
                name = "DR-ONLINE"
                url = "https://github.com/dronlinepl"
            }
        }
        scm {
            url = "https://github.com/dronlinepl/drlogger"
            connection = "scm:git:git://github.com/dronlinepl/drlogger.git"
            developerConnection = "scm:git:ssh://github.com/dronlinepl/drlogger.git"
        }
    }
}



