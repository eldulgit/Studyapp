package com.example.studyapp.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studyapp.data.model.Holiday
import com.example.studyapp.data.repository.HolidayRepository
import kotlinx.coroutines.launch

class HolidayViewModel : ViewModel() {

    private val holidayRepository = HolidayRepository()

    private val _holidays = mutableStateListOf<Holiday>()
    val holidays: List<Holiday> = _holidays  //공휴일 목록 저장

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val loadedYears = mutableSetOf<Int>()  //이미 불러온 연도는 다시 호출 x

    fun loadKoreanHolidays(year: Int) {
        if (loadedYears.contains(year)) return

        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val result = holidayRepository.getKoreanHolidays(year)

                _holidays.removeAll { holiday ->
                    holiday.date.startsWith(year.toString())
                }

                _holidays.addAll(result)

                loadedYears.add(year)
            } catch (e: Exception) {
                errorMessage = e.message ?: "공휴일 정보를 불러오지 못했습니다."
            } finally {
                isLoading = false
            }
        }
    }

    fun findHolidayByDate(date: String): Holiday? {
        return holidays.firstOrNull { holiday ->
            holiday.date == date
        }
    }
}