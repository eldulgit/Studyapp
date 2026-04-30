package com.example.studyapp.ui.stats

fun getAiStudyComment(
    currentSeconds: Int,
    previousSeconds: Int,
    goalSeconds: Int,
    period: StatsPeriod
): String {
    val currentMinutes = currentSeconds / 60
    val previousMinutes = previousSeconds / 60

    val goalPercent = if (goalSeconds > 0) {
        (currentSeconds * 100) / goalSeconds
    } else {
        0
    }

    val periodName = when (period) {
        StatsPeriod.DAILY -> "오늘"
        StatsPeriod.WEEKLY -> "최근 7일"
        StatsPeriod.MONTHLY -> "이번 달"
    }

    val previousPeriodName = when (period) {
        StatsPeriod.DAILY -> "어제"
        StatsPeriod.WEEKLY -> "이전 7일"
        StatsPeriod.MONTHLY -> "지난달"
    }

    if (currentSeconds <= 0) {
        return "🌱 ${periodName}은 아직 공부 기록이 없어요! 짧게 10분만 시작해도 충분히 좋은 출발이에요 💪"
    }

    if (goalPercent >= 100) {
        return "🎉 ${periodName} 목표를 달성했어요! 정말 잘하고 있어요. 지금 페이스 그대로 유지해봐요 ✨"
    }

    if (previousSeconds <= 0) {
        return "📚 ${periodName} ${currentMinutes}분 공부했어요! 아직 비교할 이전 기록은 없지만, 시작한 것만으로도 아주 좋아요 😊"
    }

    val diffMinutes = currentMinutes - previousMinutes

    return when {
        diffMinutes >= 30 -> {
            "🔥 ${previousPeriodName}보다 ${diffMinutes}분이나 더 공부했어요! 집중력이 점점 좋아지고 있어요. 아주 멋져요!"
        }

        diffMinutes >= 0 -> {
            "🍀 ${previousPeriodName}과 비슷한 페이스를 잘 유지하고 있어요! 조금만 더 하면 목표에 더 가까워질 수 있어요 ✨"
        }

        goalPercent >= 70 -> {
            "🌙 ${previousPeriodName}보다는 조금 줄었지만, 목표의 ${goalPercent}%까지 왔어요! 충분히 잘하고 있으니까 조금만 더 힘내봐요 😊"
        }

        else -> {
            "🐢 ${previousPeriodName}보다 ${-diffMinutes}분 줄었어요. 괜찮아요! 오늘은 짧게라도 한 번 더 집중해보면 좋아요 💪"
        }
    }
}