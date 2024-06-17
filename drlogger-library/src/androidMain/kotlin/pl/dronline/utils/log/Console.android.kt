package pl.dronline.utils.log

actual fun consolePrint(s: String) {
    println(s)
}

actual fun consoleError(s: String) {
    System.err.println(s)
}