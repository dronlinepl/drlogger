package pl.dronline.utils.log

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr

actual fun consolePrint(s: String) {
    print(s)
}

@OptIn(ExperimentalForeignApi::class)
actual fun consoleError(s: String) {
    fprintf(stderr, s)
}