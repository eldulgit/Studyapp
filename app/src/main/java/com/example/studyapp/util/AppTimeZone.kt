package com.example.studyapp.util

import java.time.ZoneId
import java.util.TimeZone

object AppTimeZone {
    val zoneId: ZoneId = ZoneId.of("Asia/Seoul")
    val timeZone: TimeZone = TimeZone.getTimeZone(zoneId)
}
