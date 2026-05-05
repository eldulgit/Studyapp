package com.example.studyapp.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
// 3번 요약 반영: LazyColumn 사용을 위한 import 추가
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.* // 개별 runtime import들을 대체하는 역할
import com.example.studyapp.ui.settings.subject.SubjectItem
import com.example.studyapp.ui.settings.subject.SubjectViewModel

/**
 * @param totalMinutes 총 공부 가능 시간 (분 단위로 설정함)
 * @param subjects     공부할 과목 리스트 (우선순위 정보 포함)
 */
fun allocateStudy(totalMinutes: Int, subjects: List<SubjectItem>): Map<String, Int> {
    // 우선 순위 높은 순으로 정렬 (숫자가 클수록 높은 우선순위라고 가정)
    val sortedSubjects = subjects.sortedByDescending { it.priority }

    val allocationMap = mutableMapOf<String, Int>()

    // 우선 순위별 배분 비율 정의 (사용자 요구사항 반영)
    // 리스트 index 0(가장 높음) -> 50%
    sortedSubjects.forEachIndexed { index, subject ->
        val percent = when (index) {
            0 -> 0.50 // 1순위 50%
            1 -> 0.30 // 2순위 30%
            2 -> 0.15 // 3순위 15%
            else -> 0.05 // 나머지는 5%
        }
        val allocated = (totalMinutes * percent).toInt()
        allocationMap[subject.name] = allocated
    }

    return allocationMap
}

// 4번 요약 확인: @Composable 어노테이션 잘 적용되어 있음
@Composable
fun StudyPlanScreen(subjectViewModel: SubjectViewModel) {
    // 1. 상태 변수 선언
    var sortOrder by remember { mutableStateOf("priority") }

    // 2. 정렬된 리스트 계산
    val displayList = when (sortOrder) {
        "name" -> subjectViewModel.subjects.sortedBy { it.name }
        "priority" -> subjectViewModel.subjects.sortedByDescending { it.priority }
        else -> subjectViewModel.subjects
    }

    // UI에서 체크박스나 Chip으로 선택
    Column {
        Row {
            FilterChip(
                selected = sortOrder == "priority",
                onClick = { sortOrder = "priority" },
                label = { Text("우선 순위 순") }
            )
            FilterChip(
                selected = sortOrder == "name",
                onClick = { sortOrder = "name" },
                label = { Text("가나다 순") }
            )
        }

        // 2번 요약 확인: allocatedTime 단수로 잘 통일되어 있음
        val allocatedTime = allocateStudy(180, displayList) //예: 180분

        LazyColumn {
            items(displayList) { subject ->
                val time = allocatedTime[subject.name] ?: 0
                Text("${subject.name} ${time}분 할당됨")
            }
        }
    }
}