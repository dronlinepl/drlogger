package pl.dronline.utils.log

import kotlin.reflect.KClass

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DrLogger actual constructor(tag: String) : DrLoggerCore(tag) {
    actual constructor(clazz: KClass<*>) : this(clazz.simpleName ?: clazz.toString())
    constructor(clazz: Class<*>) : this(clazz.simpleName ?: clazz.toString())

    actual companion object : DrLoggerImpl()
}
