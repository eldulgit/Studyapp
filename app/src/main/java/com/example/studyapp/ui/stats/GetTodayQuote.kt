package com.example.studyapp.ui.stats

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
fun getTodayQuote(): String {
    val quotes = listOf(
        "작은 노력들이 모여 큰 변화를 만듭니다.",
        "오늘의 집중이 내일의 실력을 만듭니다.",
        "완벽하지 않아도 꾸준하면 앞으로 나아갑니다.",
        "공부는 속도보다 방향과 지속성이 중요합니다.",
        "어제보다 조금 더 나아졌다면 충분히 잘하고 있는 겁니다.",
        "포기하지 않는 사람이 결국 가장 멀리 갑니다.",
        "지금 하는 10분이 미래의 나를 바꿉니다.",
        "성장은 눈에 보이지 않는 순간에도 계속되고 있습니다.",
        "오늘 할 일을 미루지 않는 것이 가장 강한 습관입니다.",
        "집중한 시간은 절대 배신하지 않습니다."
    )

    val dayOfYear = LocalDate.now().dayOfYear

    return quotes[dayOfYear % quotes.size]
}