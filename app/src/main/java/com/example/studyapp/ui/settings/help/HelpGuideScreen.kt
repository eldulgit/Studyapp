package com.example.studyapp.ui.settings.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studyapp.ui.theme.isAppInDarkTheme

@Composable
fun HelpGuideScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGuideItem by remember { mutableStateOf<HelpGuideItem?>(null) }

    val guideItems = listOf(
        HelpGuideItem(
            keyword = "스케줄",
            title = "고정 스케줄을 입력해요",
            summary = "매주 반복되는 고정 시간을 등록해요.",
            details = listOf(
                "학교 수업, 학원, 아르바이트처럼 공부 시간을 배치하면 안 되는 시간을 입력해요.",
                "요일과 시작/종료 시간을 설정하면 시간표에 고정 스케줄로 표시돼요.",
                "스케줄링할 때 이 시간은 비워두고 공부 계획을 만들어요."
            )
        ),
        HelpGuideItem(
            keyword = "목표",
            title = "목표 기간과 우선순위 체크를 설정해요",
            summary = "기간과 체크 상태가 스케줄링에 반영돼요.",
            details = listOf(
                "목표는 시작 날짜와 마감 날짜를 입력해서 기간을 정해요.",
                "목표 카드의 체크를 켜면 마감일이 가까워질수록 우선순위가 자동으로 올라가요.",
                "목표를 추가할 때는 과목과 다른 이름을 사용해주세요.",
                "기간이 아직 시작되지 않았거나 이미 지난 목표는 회색으로 표시돼요."
            )
        ),
        HelpGuideItem(
            keyword = "타이머",
            title = "재생 버튼과 카메라 버튼을 함께 사용해요",
            summary = "재생 버튼을 누른 뒤 카메라 버튼을 눌러 측정해요.",
            details = listOf(
                "먼저 과목의 재생 버튼을 눌러 측정할 과목을 선택해요.",
                "그다음 오른쪽 위 카메라 버튼을 눌러야 실제 타이머 측정이 시작돼요.",
                "Schedule에서 스케줄링을 완료하면 오늘 공부할 시간이 과목별로 자동 추가돼요.",
                "카메라 화면을 닫으면 타이머 측정도 멈춰요."
            )
        ),
        HelpGuideItem(
            keyword = "타이머 시간 수정",
            title = "과목별 시간을 직접 수정할 수 있어요",
            summary = "과목별 수정 버튼으로 오늘 시간을 조정해요.",
            details = listOf(
                "Timer 목록에서 과목별 수정 버튼을 누르면 시간을 직접 바꿀 수 있어요.",
                "자동으로 들어온 시간이 맞지 않을 때 수정하면 돼요.",
                "오늘만 공부 시간을 조정하고 싶을 때 사용할 수 있어요."
            )
        ),
        HelpGuideItem(
            keyword = "카메라 인식",
            title = "졸음과 자리비움을 인식해요",
            summary = "카메라가 집중, 졸음, 자리비움 상태를 판단해요.",
            details = listOf(
                "카메라는 얼굴 상태를 보고 집중, 졸음, 자리비움 상태를 인식해요.",
                "얼굴이 너무 가깝거나 멀면 인식이 불안정할 수 있어요.",
                "화면에 얼굴이 적당히 들어오도록 거리를 유지해 주세요.",
                "측면 얼굴이나 고개를 많이 돌린 상태에서는 측정 오류가 생길 수 있어요."
            )
        ),
        HelpGuideItem(
            keyword = "스케줄",
            title = "스케줄링 버튼으로 오늘 계획을 만들어요",
            summary = "Schedule 탭에서 오늘 공부 계획을 생성해요.",
            details = listOf(
                "Schedule 탭 오른쪽 위 달력 아이콘 옆의 스케줄링 버튼을 눌러요.",
                "과목 라벨과 시간표가 자동으로 생성돼요.",
                "생성된 시간표에 맞춰 공부하면 돼요.",
                "과목 색상 라벨과 시간표 색상이 같은 과목을 나타내요."
            )
        ),
        HelpGuideItem(
            keyword = "누적 시간",
            title = "기간별 누적 공부 시간을 확인해요",
            summary = "Daily, Weekly, Monthly로 공부 시간을 확인해요.",
            details = listOf(
                "Stats 화면 위쪽 그래프에서는 누적 공부 시간을 확인해요.",
                "Daily 필터는 일간 공부 시간을 보여줘요.",
                "Weekly 필터는 주간 공부 시간을 보여줘요.",
                "Monthly 필터는 월간 공부 시간을 보여줘요."
            )
        ),
        HelpGuideItem(
            keyword = "집중도",
            title = "시간대별 집중도를 확인해요",
            summary = "집중 상태로 인식된 시간이 시간대별로 누적돼요.",
            details = listOf(
                "시간대별 집중도 그래프는 Timer 기록을 기준으로 표시돼요.",
                "카메라가 집중하고 있는 상태로 인식한 시간이 누적돼요.",
                "어느 시간대에 집중이 잘 되는지 확인할 수 있어요.",
                "집중이 잘 되는 시간대를 보고 다음 계획을 조정해요."
            )
        ),
        HelpGuideItem(
            keyword = "과목설정",
            title = "과목명, 중요도, 색상을 저장해요",
            summary = "과목 정보를 입력하고 저장하면 카드로 표시돼요.",
            details = listOf(
                "Settings > 과목 설정에서 과목명을 입력해요.",
                "중요도를 선택하고 과목 색상을 골라요.",
                "오른쪽 위 저장 버튼을 누르면 과목이 저장돼요.",
                "저장된 과목은 화면 아래에 카드로 표시돼요."
            )
        ),
        HelpGuideItem(
            keyword = "중요도",
            title = "중요도가 높을수록 더 오래 배치돼요",
            summary = "중요도는 스케줄링 시간 배분에 영향을 줘요.",
            details = listOf(
                "중요도는 상, 중, 하 단계로 설정해요.",
                "단계가 높을수록 스케줄링할 때 해당 과목에 더 긴 공부 시간이 배정돼요.",
                "목표 카드의 우선순위 체크도 함께 켜두면 마감일이 가까운 목표가 더 중요하게 반영돼요."
            )
        )
    )
    val filteredGuideItems = remember(searchQuery, guideItems) {
        val query = searchQuery.trim()

        if (query.isBlank()) {
            guideItems
        } else {
            guideItems.filter { item ->
                item.matchesQuery(query)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }

            Text(
                text = "도움말",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Text(
            text = "궁금한 키워드를 검색하거나 카드를 눌러 자세한 가이드를 확인해요.",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            placeholder = { Text("키워드 검색") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredGuideItems.isEmpty()) {
            Text(
                text = "검색 결과가 없습니다.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            filteredGuideItems.forEach { item ->
                HelpGuideCard(
                    item = item,
                    onClick = { selectedGuideItem = item }
                )
            }
        }
    }

    selectedGuideItem?.let { item ->
        HelpGuideDialog(
            item = item,
            onDismiss = { selectedGuideItem = null }
        )
    }
}

@Composable
private fun HelpGuideDialog(
    item: HelpGuideItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column {
                Text(
                    text = item.keyword,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column {
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                item.details.forEach { detail ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}

@Composable
private fun HelpGuideCard(
    item: HelpGuideItem,
    onClick: () -> Unit
) {
    val cardContainerColor = if (isAppInDarkTheme()) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.White
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "${item.keyword} · ${item.title}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.summary,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class HelpGuideItem(
    val keyword: String,
    val title: String,
    val summary: String,
    val details: List<String>
)

private fun HelpGuideItem.matchesQuery(query: String): Boolean {
    val normalizedQuery = query.lowercase()
    return keyword.lowercase().contains(normalizedQuery) ||
            title.lowercase().contains(normalizedQuery) ||
            summary.lowercase().contains(normalizedQuery) ||
            details.any { detail -> detail.lowercase().contains(normalizedQuery) }
}
