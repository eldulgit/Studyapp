package com.example.studyapp.ai

import com.example.studyapp.ui.settings.schedule.FixedScheduleItem

// 일정 1개(제목, 시작/종료 시간 등)의 정보를 묶어두는 틀
data class TimeSlot(
    val id: String = "",              //firestore 연동을 위한 ID
    val title: String,                // 일정 제목 (예: "수학 공부")
    val startTime: String,            // 시작 시간 (예: "09:00")
    val endTime: String,              // 종료 시간 (예: "10:30")
    val subjectId: String? = null,    //어떤 과목인지 (색생 연동용)
    val isCompleted: Boolean = false, // 완료 체크 여부
    val date: String? = null          // 특정 날짜용 (고정 스케줄이 아닐 경우)
)
// 하루 단위의 상세 일정(메모, 우선순위 등 포함)을 묶어두는 틀
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
// 시작 시간과 끝나는 시간을 '분(minute) 단위의 숫자'로 단순하게 저장하는 틀
data class TimeRange(val start: Int, val end: Int)

// "수학 60분 하기"처럼 학습 목표치와 우선순위를 담아두는 틀
data class StudyRequirement(
    val name: String,
    val requiredMinutes: Int,
    val priority: Int = 1,
    val preferredStartHour: Int? = null
)
// 기상~취침 시간 사이에서 기존 일정들을 피해 '내가 쓸 수 있는 진짜 빈 시간'을 찾아내는 메인 엔진
fun getFreeTimeSlots(
    schedules: List<FixedScheduleItem>,
    wakeUpTime: String = "07:00",
    sleepTime: String = "23:00"
): List<TimeRange> {
    // "09:00" 같은 시간을 숫자(분)로 바꾼 뒤, 일정이 시작하는 시간 순서대로 차례차례 줄을 세운다.
    val busySlots = schedules.mapNotNull { schedule ->
        val start = schedule.startTime?.toMinutes() ?: return@mapNotNull null
        val end = schedule.endTime?.toMinutes() ?: return@mapNotNull null
        if (start < end) TimeRange(start, end) else null
    }.sortedBy { it.start }
    // 기상 시간과 취침 시간을 분 단위로 바꿔서, 오늘 하루 활동의 '시작 선'과 '끝 선'을 긋습니다.
    val startLimit = wakeUpTime.toMinutes() ?: 0
    val endLimit = sleepTime.toMinutes() ?: 1440

    // 2. 등록된 일정이 하나도 없는 경우 취침/기상 사이를 빈 시간으로 반환
    if (busySlots.isEmpty()) {
        return listOf(TimeRange(startLimit, endLimit))
    }

    //겹치는 일정이 있을 경우 하나로 병합하는 코드
    val mergedBusy = mutableListOf<TimeRange>()
    var current = busySlots[0]

    for (i in 1 until busySlots.size) {
        val next = busySlots[i]
        if (next.start <= current.end) {
            current = TimeRange(current.start, maxOf(current.end, next.end))
        } else {
            mergedBusy.add(current)
            current = next
        }
    }
    mergedBusy.add(current)

    //활동 시간(기상~취침) 내에서 일정 사이의 빈 공간을 찾음
    val freeSlots = mutableListOf<TimeRange>()
    var lastEnd = startLimit
    //위에서 뭉쳐둔 바쁜 일정 덩어리들을 하나씩 살펴보며 빈 공간을 찾는다.
    for (busy in mergedBusy) {
        // 일정이 기상 시간 이전이라면 건너뜀 (lastEnd 업데이트는 필요할 수도 있으나 기상 이후부터가 목표)
        if (busy.end <= startLimit) {
            continue
        }

        // 일정 시작이 기상 시간보다 이르다면 기상 시간으로 맞춤
        val effectiveStart = maxOf(busy.start, startLimit)
        //이전 일정이 끝난 직후부터 다음 일정이 시작하기 전까지 틈(여유 시간)이 있다면 바구니에 담는다.
        if (effectiveStart > lastEnd) {
            freeSlots.add(TimeRange(lastEnd, effectiveStart))
        }

        // 일정 끝이 취침 시간보다 늦다면 취침 시간으로 맞추고 종료
        if (busy.end >= endLimit) {
            lastEnd = endLimit
            break
        }
        //다음 빈 공간을 찾기 위해 '방금 끝난 일정의 시간'을 기억해
        lastEnd = maxOf(lastEnd, busy.end)
    }
    //지막 일정이 끝나고 취침 시간 전까지 시간이 남았다면, 남은 시간을 바구니에 마저 담는다.
    if (lastEnd < endLimit) {
        freeSlots.add(TimeRange(lastEnd, endLimit))
    }
    // 눌러 담은 '빈 시간 목록' 바구니를 최종 결과물로 반환
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
// 위에서 계산한 빈 시간들이 잘 나왔나 화면에 찍어서 확인해보는 테스트 도구
fun printFreeTimes(fixedScheduleList: List<FixedScheduleItem>) {
    val freeTimes = getFreeTimeSlots(fixedScheduleList)
    freeTimes.forEach {
        //println("빈 시간: ${it.start}분 ~ ${it.end}분 (총 ${it.end - it.start}분)")
        println("빈 시간: ${it.
        toFormattedString()} (총 ${it.end - it.start}분)")
    }
}
// 컴퓨터가 계산한 분 단위 숫자를 다시 우리가 읽기 편한 "09:30 ~ 11:00" 형태로 포장해 주는 도구
fun TimeRange.toFormattedString(): String {
    val startHour = (start / 60).toString().padStart(2, '0')
    val startMin = (start % 60).toString().padStart(2, '0')
    val endHour = (end / 60).toString().padStart(2, '0')
    val endMin = (end % 60).toString().padStart(2, '0')
    return "$startHour:$startMin ~ $endHour:$endMin"
}