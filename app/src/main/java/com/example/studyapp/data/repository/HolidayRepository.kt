package com.example.studyapp.data.repository

import com.example.studyapp.data.AppConstants
import com.example.studyapp.data.model.Holiday
import com.example.studyapp.data.remote.RetrofitClient

class HolidayRepository {

    suspend fun getKoreanHolidays(year: Int): List<Holiday> {
        return RetrofitClient.holidayApi.getPublicHolidays(
            year = year,
            countryCode = AppConstants.DEFAULT_COUNTRY_CODE
        )
    }
}