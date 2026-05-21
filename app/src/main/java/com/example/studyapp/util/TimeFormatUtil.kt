package com.example.studyapp.util

fun filterTimeInput(input: String): String {
    return input
        .filter { char ->
            char.isDigit() ||
                    char == ':' ||
                    char == '：' ||
                    char == '﹕' ||
                    char == '꞉' ||
                    char == '.' ||
                    char == '시' ||
                    char == '분' ||
                    char.isWhitespace()
        }
        .take(8)
}

fun normalizeTimeInput(input: String): String? {
    val raw = input
        .trim()
        .replace('：', ':')
        .replace('﹕', ':')
        .replace('꞉', ':')
        .replace('.', ':')
        .replace("시", ":")
        .replace("분", "")
        .filterNot(Char::isWhitespace)

    if (raw.isBlank()) return null

    if (raw.contains(":")) {
        val parts = raw.split(":")
        if (parts.size != 2 || parts.any(String::isBlank)) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        return formatTimeOrNull(hour, minute)
    }

    if (!raw.all(Char::isDigit)) return null

    val (hour, minute) = when (raw.length) {
        1, 2 -> raw.toInt() to 0
        3 -> raw.substring(0, 1).toInt() to raw.substring(1, 3).toInt()
        4 -> raw.substring(0, 2).toInt() to raw.substring(2, 4).toInt()
        else -> return null
    }

    return formatTimeOrNull(hour, minute)
}

private fun formatTimeOrNull(hour: Int, minute: Int): String? {
    if (hour == 24 && minute == 0) return "00:00"
    if (hour !in 0..23) return null
    if (minute !in 0..59) return null

    return hour.toString().padStart(2, '0') +
            ":" +
            minute.toString().padStart(2, '0')
}
