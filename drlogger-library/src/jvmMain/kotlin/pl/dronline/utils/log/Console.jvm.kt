package pl.dronline.utils.log

actual fun consolePrint(s: String) {
    print(s)
}

actual fun consoleError(s: String) {
    System.err.print(s)
}