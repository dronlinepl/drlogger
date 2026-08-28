# DR-Logger

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/pl.dronline.multiplatform.utils/drlogger)](https://central.sonatype.com/search?namespace=pl.dronline.multiplatform.utils)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub](https://img.shields.io/badge/GitHub-drlogger--library-blue?logo=github)](https://github.com/dronlinepl/drlogger-library)

A powerful **Kotlin Multiplatform logging library** designed for cross-platform applications. DR-Logger provides a
flexible, listener-based logging architecture with platform-specific implementations for Android, iOS, JVM, macOS,
Linux, and Windows.

## Features

- **Multiplatform Support**: Single codebase for Android, iOS, JVM, macOS, Linux, and Windows
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
    - Windows: Windows Event Log integration
- **Built-in Listeners**:
    - Console output with emoji indicators
    - Daily rotating file logs with automatic cleanup and size-based rotation
    - Syslog integration (Linux/macOS → syslog, Windows → Event Log)
    - Platform-specific system loggers (Logcat, Log4j, Event Log)

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

### Short-Lived Processes

Logs are processed asynchronously. CLI tools, `oneshot` services, and other short-lived processes should flush pending messages and stop listeners before exiting:

```kotlin
try {
    DrLogger("Updater").info("Update completed")
} finally {
    DrLogger.shutdownBlocking()
}
```

The suspending `DrLogger.shutdown()` variant is available for coroutine-based code. Use `DrLogger.flush()` or `DrLogger.flushBlocking()` when listeners should remain active.

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

val fileListener = DailyFileLogListener().apply {
    path = "/path/to/logs"   // Directory is created automatically if it doesn't exist
    namePrefix = "app-"      // Log files: app-20250123.log
    maxFileCount = 30        // Keep max 30 files
    maxFileAgeDays = 90      // Delete files older than 90 days
    maxFileSize = 10.MB      // Optional: 10MB max per file (nginx-style rotation)
    enabled = true
}

DrLogger.addListener(fileListener)
```

When `maxFileSize` is set, files rotate nginx-style within a day:
- `app-20250123.log` - current file (always newest)
- `app-20250123.1.log` - previous file
- `app-20250123.2.log` - older file

Available size units: `10.bytes`, `500.kB`, `10.MB`, `1.GB`

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

Use `SyslogLogListener` for system logging:

```kotlin
DrLogger.addListener(SyslogLogListener(
    ident = "myapp",
    facility = SyslogFacility.USER
).apply { enabled = true })
```

View logs with: `journalctl -t myapp` or `tail -f /var/log/syslog`

### Windows

No additional setup required. Use `LogcatLogListener` or `SyslogLogListener` to write to Windows Event Log.
Both delegate to `EventLogLogListener` internally.

View logs using Event Viewer (`eventvwr.msc`) under "Windows Logs" -> "Application":

```kotlin
// Using LogcatLogListener (default source: "DrLogger")
DrLogger.addListener(LogcatLogListener().apply { enabled = true })

// Or using SyslogLogListener with custom source name
DrLogger.addListener(SyslogLogListener(ident = "MyApp").apply { enabled = true })
```

Log levels are mapped to Event Log types:
- FATAL/ERROR -> Error
- WARN -> Warning
- INFO/DEBUG/TRACE -> Information

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
| Windows  | `mingwX64`                                | Windows 7+           |

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

- **JDK 17 or higher** (Java 17 is required for the build)
- **Kotlin 2.2.21+** (included via Gradle)
- **Android SDK** (for Android targets) - set `ANDROID_HOME` environment variable
- **Xcode** (for iOS/macOS targets)


### Platform-Specific Notes

- **iOS/macOS targets** can only be built on macOS with Xcode installed
- **Android targets** require Android SDK
- **Linux targets** can be built on any platform but require libsystemd-dev for native features
- **Windows targets** can be built on any platform using MinGW toolchain


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

- **DR-ONLINE SP. Z O.O.** - Copyright (c) 2017-2026
- **Przemysław Dobrowolski** - Copyright (c) 2017-2026

## Acknowledgments

Built with:

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlinx Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Kotlinx DateTime](https://github.com/Kotlin/kotlinx-datetime)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [Apache Log4j](https://logging.apache.org/log4j/) (JVM platform)

## Support

For questions, issues, or feature requests:
- Open an issue on [GitHub](https://github.com/dronlinepl/drlogger-library/issues)
- Visit [DR-ONLINE](https://www.dr-online.pl) 
