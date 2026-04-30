package com.example.studyapp.ai

import kotlin.math.abs
// 가장 적합한 공부 추천받기
// 빈 시간 슬롯 하나에 대해 여러 과목 중 어떤 게 제일 잘 맞는지 찾아주는 함수

fun recommendBestStudy(
    slot: TimeRange,
    requirements: List<StudyRequirement>
): Pair<StudyRequirement?, Double> {
    var bestMatch: StudyRequirement? = null
    var maxScore = -1.0

    for (req in requirements) {
        val score = calculateSuitability(slot, req)
        if (score > maxScore) {
            maxScore = score
            bestMatch = req
        }
    }
    return Pair(bestMatch, maxScore)
}
// 이 코드가 해내는 일
// 1. 시간 엄수: 공부에 필요한 시간보다 빈 시간이 적으면 가차 없이 0점을 줘서 잘못된 추천을 방지한다.
// 2. 효율 극대화: 시간이 딱 맞는 슬롯(10분 이내 여유)에 더 높은 점수를 주어 자투리 시간을 낭비하지 않게 한다.
// 3. 사용자 맞춤: 사용자가 선호하는 시간대(아침, 오후 등)를 반영하여 가장 기분 좋게 공부할 수 있는 시간을 찾아준다.
// 4. 최적의 선택: 여러 과목 후보 중 점수가 가장 높은 과목 하나를 콕 집어 추천해 준다.