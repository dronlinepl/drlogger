class CommonLoggerTest {
//
//    @Test
//    @OptIn(ExperimentalTime::class)
//    fun logToJournal() {
//        // nie będzie kompilować się pod androidem, bo DaylyFileLoglistener musi być z context.
//        runBlocking {
//            DrLogger.addListener(LogcatLogListener(), DailyFileLogListener().apply {
//                enabled = true
//                path = "/tmp"
//                namePrefix = "test_"
//            })
//
//            DrLogger("TEST").info("Hello world")
//
//            DrLogger("TEST").info("TEST INFO")
//            DrLogger("TEST").warn("TEST WARNING")
//            DrLogger("TEST").error("TEST ERROR")
//            DrLogger("TEST").debug("TEST DEBUG")
//            DrLogger("TEST").error(Exception("TEST"), "TEST EXCEPTION")
//
//            delay(10000)
//        }
//    }

}