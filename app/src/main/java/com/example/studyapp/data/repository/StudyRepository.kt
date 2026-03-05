package com.example.studyapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.studyapp.ui.calendar.StudyRecord
import java.time.LocalDate
import java.time.YearMonth

object StudyRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    private val fakeStudyRecords = listOf(
        StudyRecord(LocalDate.of(2026, 1, 4), 20),
        StudyRecord(LocalDate.of(2026, 1, 5), 80),
        StudyRecord(LocalDate.of(2026, 1, 10), 150)
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun getStudyMinutesForDate(date: LocalDate): Int {
        return fakeStudyRecords
            .filter { it.date == date }
            .sumOf { it.minutes }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthTotal(yearMonth: YearMonth): Int {
        return fakeStudyRecords
            .filter { YearMonth.from(it.date) == yearMonth }
            .sumOf { it.minutes }
    }
}