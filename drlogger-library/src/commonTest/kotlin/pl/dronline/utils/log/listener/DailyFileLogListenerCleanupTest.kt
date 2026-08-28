package pl.dronline.utils.log.listener

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class DailyFileLogListenerCleanupTest {
    @Test
    fun retentionCutoffDate_dstTransition_usesCalendarDays() {
        val cutoffDate = retentionCutoffDate(
            now = Instant.parse("2026-10-26T22:30:00Z"),
            maxFileAgeDays = 90,
            timeZone = TimeZone.of("Europe/Warsaw"),
        )

        assertEquals("20260728", cutoffDate)
    }
}
