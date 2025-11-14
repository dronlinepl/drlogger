# DR-Logger

A powerful **Kotlin Multiplatform logging library** designed for cross-platform applications. DR-Logger provides a
flexible, listener-based logging architecture with platform-specific implementations for Android, iOS, JVM, macOS, and
Linux.

## Features

- **Multiplatform Support**: Single codebase for Android, iOS, JVM, macOS, and Linux
- **Flexible Listener Architecture**: Route log messages to multiple outputs simultaneously
- **Multiple Log Levels**: DEBUG, TRACE, INFO, WARN, ERROR, FATAL with configurable filtering
- **Asynchronous Processing**: Non-blocking log delivery using Kotlin coroutines
- **Advanced Filtering**: Regex-based message and tag filtering
- **Thread-Safe**: Built with mutex locks and atomic operations for concurrent access
- **Platform-Specific Integrations**:
    - Android: Logcat integration
    - JVM: Apache Log4j support
    - Linux: Systemd logging support
    - iOS/macOS: Native platform logging
- **Built-in Listeners**:
    - Console output with emoji indicators
    - Daily rotating file logs with automatic cleanup
    - Platform-specific system loggers

## Installation

### Gradle (Kotlin DSL)

Add the DR-Logger dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("pl.dronline.multiplatform.utils:drlogger-library:0.5.+")
}
```

### Maven Repository

Add the repository to your build configuration:

```kotlin
repositories {
    maven {
        url = uri("https://mvn.dr-online.pl/artifactory/libs-release-local")
    }
}
```

## Quick Start

### Basic Usage

```kotlin
import pl.dronline.utils.log.DrLogger
import pl.dronline.utils.log.listener.EmojiConsoleLogListener

// Add a console listener
DrLogger.addListener(EmojiConsoleLogListener().apply {
    enabled = true
})

// Create logger instance
val logger = DrLogger("MyApp")

// Log messages
logger.debug("Application started")
logger.info("User logged in")
logger.warn("Memory usage high")
logger.error("Failed to connect to server")
logger.fatal("Critical system failure")
```

### Short Form Methods

```kotlin
logger.d("TAG", "Debug message")
logger.i("TAG", "Info message")
logger.w("TAG", "Warning message")
logger.e("TAG", "Error message")
```

### Logging Exceptions

```kotlin
try {
    // Some code that might throw
} catch (e: Exception) {
    logger.error(e, "Failed to process request")
}
```

### Custom Listeners

Create custom log listeners by implementing `ILogListener`:

```kotlin
class CustomLogListener : ALogListener("CustomListener") {
    override suspend fun onLogMessage(message: LogMessage) {
        // Handle log message
        println("[${message.level}] ${message.tag}: ${message.message}")
    }
}

// Add to logger
DrLogger.addListener(CustomLogListener().apply { enabled = true })
```

### File Logging with Daily Rotation

```kotlin
import pl.dronline.utils.log.listener.DailyFileLogListener
import java.io.File

val fileListener = DailyFileLogListener(
    logDir = File("/path/to/logs"),
    filePrefix = "app",
    maxDays = 7  // Keep logs for 7 days
).apply { enabled = true }

DrLogger.addListener(fileListener)
```

### Filtering

```kotlin
// Filter by log level
listener.minLevel = DrLogLevel.WARN

// Filter by tag pattern
listener.tagFilter = Regex("^MyApp.*")

// Filter by message pattern
listener.messageFilter = Regex(".*error.*", RegexOption.IGNORE_CASE)
```

## Platform-Specific Setup

### Android

No additional setup required. Logcat integration works out of the box:

```kotlin
DrLogger.addListener(LogcatLogListener().apply { enabled = true })
```

### JVM (Backend/Desktop)

For Log4j integration:

```kotlin
DrLogger.addListener(Log4jLogListener().apply { enabled = true })
```

Make sure Log4j is configured in your application.

### Linux

Install required system dependencies:

```bash
sudo apt install libsystemd-dev gcc-multilib
```

### iOS/macOS

No additional setup required. Native platform logging is automatically configured.

## Supported Platforms

| Platform | Target                                    | Min Version          |
|----------|-------------------------------------------|----------------------|
| Android  | `androidTarget`                           | API 24 (Android 7.0) |
| iOS      | `iosArm64`, `iosX64`, `iosSimulatorArm64` | iOS 12+              |
| JVM      | `jvm`                                     | Java 8+              |
| macOS    | `macosArm64`, `macosX64`                  | macOS 10.13+         |
| Linux    | `linuxX64`, `linuxArm64`                  | glibc 2.27+          |

## Architecture

DR-Logger uses a central dispatch system (`DrLoggerFactory`) that routes log messages to registered listeners. Each
listener runs independently with its own coroutine scope, ensuring non-blocking operation.

```
┌──────────────┐
│   DrLogger   │
└──────┬───────┘
       │
       v
┌──────────────────┐       ┌─────────────────┐
│ DrLoggerFactory  │──────>│  ILogListener   │
└──────────────────┘       └─────────────────┘
       │                            │
       ├────────────────────────────┼──────────────┐
       v                            v              v
┌─────────────┐         ┌────────────────┐  ┌──────────┐
│   Console   │         │   File Logger  │  │  Logcat  │
└─────────────┘         └────────────────┘  └──────────┘
```

## Configuration

### Global Settings

```kotlin
// Set minimum log level globally
DrLogger.minLevel = DrLogLevel.INFO

// Enable/disable specific listeners
listener.enabled = true
```

### Buffer Configuration

DR-Logger uses a 512-element buffer for log messages. Messages are processed asynchronously to avoid blocking the
calling thread.

## Building from Source

### Prerequisites

- JDK 17 or higher
- Kotlin 2.2.21+
- Android SDK (for Android targets)
- Xcode (for iOS/macOS targets, macOS only)

### Build

```bash
# Build all targets
./gradlew build

# Build specific target
./gradlew jvmTest
./gradlew androidDebug

# Publish to local Maven
./gradlew publishToMavenLocal
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Copyright (c) 2017-2025 DR-ONLINE SP. Z O.O.
Copyright (c) 2017-2025 Przemysław Dobrowolski

## Acknowledgments

Built with:

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Apache Log4j](https://logging.apache.org/log4j/)
- [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)

---

For more information, visit [DR-ONLINE](https://www.dr-online.pl)