package com.example.studyapp.ai

import com.example.studyapp.ui.settings.schedule.FixedScheduleItem
fun generateFreeTimeObjects(
    freeRanges: List<TimeRange>,
    currentDate: String? = null,
    minMinutes: Int = 10 //최소 10분 이상인 시간만
): List<TimeSlot> {
    return freeRanges
        .filter { (it.end - it.start) >= minMinutes } // 너무 짧은 시간은 제외
        .map { range ->
            //분 단위를 HH:mm 문자열로 변환
            val startStr = String.format("%02d:%02d", range.start / 60, range.start % 60)
            val endStr = String.format("%02d:%02d", range.end / 60, range.end % 60)

            TimeSlot(
                id = "FREE_${range.start}_${range.end}",
                title = "비어 있는 시간",
                startTime = startStr,
                endTime = endStr,
                subjectId = "FREE_SLOT", //구분용 ID
                isCompleted = false,
                date = currentDate
            )
        }
}

fun generateDailyFreeItems(
    freeRanges: List<TimeRange>,
    date: String
): List<DailyScheduleItem> {
    return freeRanges.map { range ->
        val startStr = String.format("%02d:%02d", range.start / 60, range.start % 60)
        val endStr = String.format("%02d:%02d", range.end / 60, range.end % 60)

        DailyScheduleItem(
            id = "FREE_${date}_${range.start}",
            date = date,
            subjectId = null,
            title = "자유시간 (${range.end - range.start}분)",
            startTime = startStr,
            endTime = endStr,
            isCompleted = false,
            memo = "이 시간대에 새로운 일정을 추가할 수 있습니다.",
            priority = 1
        )
    }
}

fun collectEmptyTimeSlots(fixedSchedules: List<FixedScheduleItem>, date: String): List<TimeSlot> {
    //일정 외의 시간 범위 계산
    val freeRanges = getFreeTimeSlots(fixedSchedules)
    //계산된 범위를 TimeSlot 객체로 생성
    return generateFreeTimeObjects(freeRanges, date)
}
// 오늘 날짜의 빈 시간 슬롯 리스트를 가져와서 UI에 보여주기
// val emptySlots = collectEmptyTimeSlots(fixedScheduleList, "2024-04-30")