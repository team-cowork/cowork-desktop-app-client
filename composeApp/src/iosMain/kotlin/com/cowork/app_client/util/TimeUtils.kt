package com.cowork.app_client.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.timeZoneForSecondsFromGMT

internal actual fun nowPlusHoursIso8601(hours: Double): String {
    val date = NSDate.dateWithTimeIntervalSinceNow(hours * 3600.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    formatter.timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
    return formatter.stringFromDate(date)
}
