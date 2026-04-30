package com.example.studyapp.ai

import com.example.studyapp.ui.settings.schedule.FixedScheduleItem

data class TimeSlot(
    val id: String = "",              //firestore 연동을 위한 ID
    val title: String,                // 일정 제목 (예: "수학 공부")
    val startTime: String,            // 시작 시간 (예: "09:00")
    val endTime: String,              // 종료 시간 (예: "10:30")
    val subjectId: String? = null,    //어떤 과목인지 (색생 연동용)
    val isCompleted: Boolean = false, // 완료 체크 여부
    val date: String? = null          // 특정 날짜용 (고정 스케줄이 아닐 경우)
)
data class DailyScheduleItem(
    val id: String,                    // 고유 ID
    val date: String,                  // 날짜 (YYYY-MM-DD)
    val subjectId: String?,            // 과목 ID (Nullable: 과목 없는 일정 가능)
    val title: String,                 // 일정 제목
    val startTime: String,             // 시작 시간 (HH:mm)
    val endTime: String,               // 종료 시간 (HH:mm)
    val isCompleted: Boolean = false,  // 완료 여부
    val memo: String = "",             // 간단한 메모
    val priority: Int = 1              // 우선순위 (1: 낮음, 2: 보통, 3: 높음)
)
data class TimeRange(val start: Int, val end: Int)

data class StudyRequirement(
    val name: String,
    val requiredMinutes: Int,
    val priority: Int = 1,
    val preferredStartHour: Int? = null
)

fun getFreeTimeSlots(schedules: List<FixedScheduleItem>): List<TimeRange> {
    // 1. 변수명을 busySlots로 통일하고, mapNotNull의 대소문자 오타 수정
    val busySlots = schedules.mapNotNull { schedule ->
        val start = schedule.startTime?.toMinutes() ?: return@mapNotNull null
        val end = schedule.endTime?.toMinutes() ?: return@mapNotNull null
        if (start < end) TimeRange(start, end) else null
    }.sortedBy { it.start }

    val dayEnd = 24 * 60 // 1440분 (24:00)

    // 2. 등록된 일정이 하나도 없는 경우 하루 전체를 빈 시간으로 반환
    if (busySlots.isEmpty()) {
        return listOf(TimeRange(0, dayEnd))
    }

    //겹치는 일정이 있을 경우 하나로 병합하는 코드
    val mergedBusy = mutableListOf<TimeRange>()
    var current = busySlots[0]

    for (i in 1 until busySlots.size) {
        val next = busySlots[i]
        // <= 로 처리하면 끝나는 시간과 다음 시작 시간이 딱 맞물릴 때도 하나의 바쁜 시간으로 자연스럽게 병합됩니다.
        if (next.start <= current.end) {
            current = TimeRange(current.start, maxOf(current.end, next.end))
        } else {
            mergedBusy.add(current)
            current = next
        }
    } //마지막 남은 current를 리스트에 추가
    mergedBusy.add(current)

    //하루 전체에서 일정 사이의 빈 공간을 찾음
    val freeSlots = mutableListOf<TimeRange>()
    var lastEnd = 0  // 00:00부터 시작

    for (busy in mergedBusy) {
        if (busy.start > lastEnd) {
            // 이전 일정 끝과 현재 일정 시작 사이에 빈 공간이 있다면 추가 (오타 수정)
            freeSlots.add(TimeRange(lastEnd, busy.start))
        }
        lastEnd = maxOf(lastEnd, busy.end)
    }
    //마지막 일정 이후에서 24:00까지 남은 시간이 있다면? 을 추가
    if (lastEnd < dayEnd) {
        freeSlots.add(TimeRange(lastEnd, dayEnd))
    }

    return freeSlots
}
    // "HH.MM"을 분 단위로 바꾸는 확장 함수에요.
    fun String.toMinutes(): Int? {
        val parts = split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return hour * 60 + minute
    }

fun printFreeTimes(fixedScheduleList: List<FixedScheduleItem>) {
    val freeTimes = getFreeTimeSlots(fixedScheduleList)
    freeTimes.forEach {
        //println("빈 시간: ${it.start}분 ~ ${it.end}분 (총 ${it.end - it.start}분)")
        println("빈 시간: ${it.toFormattedString()} (총 ${it.end - it.start}분)")
    }
}
fun TimeRange.toFormattedString(): String {
    val startHour = (start / 60).toString().padStart(2, '0')
    val startMin = (start % 60).toString().padStart(2, '0')
    val endHour = (end / 60).toString().padStart(2, '0')
    val endMin = (end % 60).toString().padStart(2, '0')
    return "$startHour:$startMin ~ $endHour:$endMin"
}