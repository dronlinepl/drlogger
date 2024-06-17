plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.jfrogArtifactory)
    alias(libs.plugins.dokka)
    `maven-publish`
}

val longVersion = "0.4.${gitRevisionStrNr}"
version = longVersion


kotlin {
    targetHierarchy.default()
    jvm()

    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        //TODO: Sprawdzić czy if jest potrzebny dla linuxa.
        iosX64()
        iosArm64()
        iosSimulatorArm64()
        macosArm64()
        macosX64()
    }
    //if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
    //a mac
    linuxX64()
    linuxArm64()
    //}

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

//        val androidMain by getting {
//            dependencies {
//                implementation(libs.log4j.core)
//            }
//        }
    }
}

android {
    namespace = "pl.dronline.log"
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