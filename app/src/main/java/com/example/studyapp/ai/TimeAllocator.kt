package com.example.studyapp.ai

import kotlin.math.abs

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
    val sortedRequirements = requirements.sortedWith(
        compareByDescending<StudyRequirement> { it.priority }
            .thenByDescending { it.requiredMinutes }
    )
    val remainingSlots = freeSlots.toMutableList()
    val finalAssignments = mutableListOf<Pair<TimeRange, StudyRequirement>>()
    for (req in sortedRequirements) {
        var bestSlotIndex = -1
        var maxScore = -1.0
        // 가장 적합한 슬롯 찾기
        for (i in remainingSlots.indices) {
            val score = calculateSuitability(remainingSlots[i], req)
            if (score > 0 && score > maxScore) {
                maxScore = score
                        bestSlotIndex = i
            }
        }
        // 3. 최적의 슬롯을 찾았다면 배정 및 자투리 처리
        if (bestSlotIndex != -1) {
            val selectedSlot = remainingSlots.removeAt(bestSlotIndex)
            // 공부에 필요한 시간만큼만 딱 자름
            val assignedRange = TimeRange(selectedSlot.start, selectedSlot.start + req.requiredMinutes)
            finalAssignments.add(assignedRange to req)
            // 남은 자투리 시간이 있다면 다시 목록에 넣어서 다음 과목이 쓸 수 있게 함
            val leftoverStart = assignedRange.end
            val leftoverEnd = selectedSlot.end
            if (leftoverStart < leftoverEnd) {
                remainingSlots.add(bestSlotIndex, TimeRange(leftoverStart, leftoverEnd))
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
            subjectId = null, // subjectId 누락 해결
            isCompleted = false,
            date = date
            )
        }
    }