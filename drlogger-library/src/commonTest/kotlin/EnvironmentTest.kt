import pl.dronline.utils.Environment
import kotlin.test.Test

class EnvironmentTest {
    @Test
    fun test() {
        val x = Environment.get("PATH")

        println("Environment: $x")
    }
}