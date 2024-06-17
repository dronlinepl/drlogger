package pl.dronline.utils.datetime

import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

@OptIn(FormatStringsInDatetimeFormats::class)
fun Instant.toString(fmt : String) : String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    val format = LocalDateTime.Format { byUnicodePattern(fmt) }

    return dateTime.format(format)
}