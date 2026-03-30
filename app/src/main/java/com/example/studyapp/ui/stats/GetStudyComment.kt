package com.example.studyapp.ui.stats

fun getStudyComment(
    percent: Int,
    period: StatsPeriod
): String {

    return when (period) {

        StatsPeriod.DAILY -> when {
            percent >= 100 -> "오늘 목표 완벽 달성! 🎯"
            percent >= 60 -> "오늘 집중 잘했어요 👍"
            else -> "오늘은 가볍게 했네요 😅"
        }

        StatsPeriod.WEEKLY -> when {
            percent >= 100 -> "이번 주 정말 꾸준했어요 🔥"
            percent >= 70 -> "주간 흐름 좋아요 💪"
            else -> "이번 주는 조금 아쉬워요 📉"
        }

        StatsPeriod.MONTHLY -> when {
            percent >= 100 -> "이번 달 성장 폭발 🚀"
            percent >= 70 -> "한 달 동안 잘 유지했어요 👏"
            else -> "다음 달엔 더 올라가요 💡"
        }
    }
}
