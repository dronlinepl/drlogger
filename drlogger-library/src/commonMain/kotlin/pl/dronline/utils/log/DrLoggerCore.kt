package pl.dronline.utils.log

abstract class DrLoggerCore(private val tag: String) {

    /**
     *
     */
    fun wtf(tagParam: String, msg: String) {
        DrLoggerFactory.fatal(tagParam, msg)
    }

    /**
     *
     */
    fun wtf(tagParam: String, th: Throwable, msg: String) {
        DrLoggerFactory.fatal(tagParam, msg, th)
    }

    fun fatal(msg: String) {
        DrLoggerFactory.fatal(tag, msg)
    }

    fun fatal(th: Throwable, msg: String) {
        DrLoggerFactory.fatal(tag, msg, th)
    }

    /**
     *
     */
    fun d(tagParam: String, msg: String) {
        DrLoggerFactory.debug(tagParam, msg)
    }

    /**
     *
     */
    fun debug(msg: String) {
        debug { msg }
    }

    /**
     *
     */
    fun debug(msg: () -> String) {
        DrLoggerFactory.debug(tag, msg.invoke())
    }

    /**
     *
     */
    fun t(tagParam: String, msg: String) {
        DrLoggerFactory.trace(tagParam, msg)
    }

    fun trace(msg: String) {
        trace { msg }
    }

    /**
     *
     */
    fun trace(msg: () -> String) {
        DrLoggerFactory.trace(tag, msg.invoke())
    }

    /**
     *
     */
    fun i(tagParam: String, msg: String) {
        DrLoggerFactory.info(tagParam, msg)
    }

    /**
     *
     */
    fun i(tagParam: String, th: Throwable, msg: String) {
        DrLoggerFactory.info(tagParam, msg, th)
    }


    fun info(msg: String) {
        info { msg }
    }


    fun info(msg: () -> String) {
        DrLoggerFactory.info(tag, msg.invoke())
    }

    fun info(t: Throwable, msg: String?) {
        DrLoggerFactory.info(tag, msg ?: "null", t)
    }

    /**
     *
     */
    fun w(tagParam: String, msg: String) {
        DrLoggerFactory.warn(tagParam, msg)
    }

    /**
     *
     */
    fun w(tagParam: String, th: Throwable, msg: String) {
        DrLoggerFactory.warn(tagParam, msg, th)
    }

    fun warn(msg: String) {
        warn { msg }
    }

    fun warn(msg: () -> String) {
        DrLoggerFactory.warn(tag, msg.invoke())
    }

    fun warn(t: Throwable, msg: String?) {
        DrLoggerFactory.warn(tag, msg ?: "null", t)
    }

    /**
     *
     */
    fun e(tagParam: String, msg: String) {
        DrLoggerFactory.error(tagParam, msg)
    }

    /**
     *
     */
    fun e(tagParam: String, th: Throwable, msg: String) {
        DrLoggerFactory.error(tagParam, msg, th)
    }

    fun error(msg: String) {
        error { msg }
    }

    fun error(msg: () -> String) {
        DrLoggerFactory.error(tag, msg.invoke())
    }

    fun error(t: Throwable, msg: String) {
        DrLoggerFactory.error(tag, msg ?: "null", t)
    }
}