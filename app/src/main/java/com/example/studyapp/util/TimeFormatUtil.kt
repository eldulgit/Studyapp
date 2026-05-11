package com.example.studyapp.util

fun normalizeTimeInput(input: String): String? {
    val raw = input
        .trim()
        .replace("：", ":") // 혹시 전각 콜론 입력했을 때 대비

    if (raw.isBlank()) return null

    // 이미 07:00, 7:00, 23:30 형식으로 입력한 경우
    if (raw.contains(":")) {
        val parts = raw.split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        if (hour !in 0..23) return null
        if (minute !in 0..59) return null

        return formatTime(hour, minute)
    }

    // 7, 07, 700, 730, 1230 같은 숫자만 입력한 경우
    if (!raw.all { it.isDigit() }) return null

    val hour: Int
    val minute: Int

    when (raw.length) {
        1, 2 -> {
            // 7 -> 07:00
            // 12 -> 12:00
            hour = raw.toInt()
            minute = 0
        }

        3 -> {
            // 700 -> 07:00
            // 730 -> 07:30
            hour = raw.substring(0, 1).toInt()
            minute = raw.substring(1, 3).toInt()
        }

        4 -> {
            // 0730 -> 07:30
            // 1230 -> 12:30
            hour = raw.substring(0, 2).toInt()
            minute = raw.substring(2, 4).toInt()
        }

        else -> return null
    }

    if (hour !in 0..23) return null
    if (minute !in 0..59) return null

    return formatTime(hour, minute)
}

private fun formatTime(hour: Int, minute: Int): String {
    return hour.toString().padStart(2, '0') +
            ":" +
            minute.toString().padStart(2, '0')
}