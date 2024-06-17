package pl.dronline.utils.log

import kotlin.reflect.KClass

actual class DrLogger actual constructor(tag: String) : DrLoggerCore(tag) {
    actual constructor(clazz: KClass<*>) : this(clazz.simpleName ?: clazz.toString())

    actual companion object : DrLoggerImpl()
}
