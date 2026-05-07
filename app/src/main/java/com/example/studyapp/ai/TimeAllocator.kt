package com.example.studyapp.ai

import kotlin.math.abs

/*data class TimeRange(val start: Int, val end: Int)
data class StudyRequirement(
    val name: String,
    val priority: Int,
    val requiredMinutes: Int,
    val preferredStartHour: Int? = null
)*/
 // 적합도 계산 함수
 fun calculateSuitability(slot: TimeRange, requirement: StudyRequirement): Double {
     val slotDuration = slot.end - slot.start
     // 시간 부족 시 0점
     if (slotDuration < requirement.requiredMinutes) return 0.0

     var score = 50.0 // 기본 점수
     val remainingTime = slotDuration - requirement.requiredMinutes
     // 밀착도 점수 (최대 30점)
     score += when {
        remainingTime <= 10 -> 30.0
        remainingTime <= 30 -> 20.0
        remainingTime <= 60 -> 10.0
        else -> 5.0
    }
    // 선호 시간대 점수 (최대 20점)
     requirement.preferredStartHour?.let { preferred ->
        val startHour = slot.start / 60
        val diff = abs(startHour - preferred)
        score += when {
            diff == 0 -> 20.0
            diff <= 2 -> 10.0
            else -> 0.0
        }
    }
    return score // 반드시 점수를 반환해야함.
}

// 우선순위 기반 자동 배치 알고리즘
fun arrangeSchedulesByPriority(
    freeSlots: List<TimeRange>,
    requirements: List<StudyRequirement>
): List<Pair<TimeRange, StudyRequirement>> {
    // 우선순위 높은 순 -> 소요시간 긴 순으로 정렬
    val sortedReqs = requirements.sortedByDescending { it.priority }

    val availableSlots = freeSlots.sortedBy { it.start }.toMutableList()

    val finalAssignments = mutableListOf<Pair<TimeRange, StudyRequirement>>()

    for (req in sortedReqs) {
        var remainingMin = req.requiredMinutes

        while (remainingMin > 0 && availableSlots.isNotEmpty()) {
            val slot = availableSlots.removeAt(0)
            val slotDuration = slot.end - slot.start

            if (slotDuration <= remainingMin) {
                finalAssignments.add(slot to req)
                remainingMin -= slotDuration
            } else {
                val studyRange = TimeRange(slot.start, slot.start + remainingMin)
                finalAssignments.add(studyRange to req)
                availableSlots.add(0, TimeRange(slot.start + remainingMin, slot.end))
                remainingMin = 0
            }
        }
    }
    return finalAssignments
}
// 최종 시간표 객체 생성
fun getFinalTimeTable(
    freeSlots: List<TimeRange>,
    requirements: List<StudyRequirement>,
    date: String
): List<TimeSlot> {
    val assignments = arrangeSchedulesByPriority(freeSlots, requirements)
    return assignments.map { (range, req) ->
        val startStr = String.format("%02d:%02d", range.start / 60, range.start % 60)
        val endStr = String.format("%02d:%02d", range.end / 60, range.end % 60)
        // TimeSlot의 필드 순서와 타입에 맞춰서 생성
        TimeSlot(
                id = "PLAN_${date}_${range.start}",
            title = req.name,
            startTime = startStr,
            endTime = endStr,
            subjectId = null,
            isCompleted = false,
            date = date
            )
        }
    }