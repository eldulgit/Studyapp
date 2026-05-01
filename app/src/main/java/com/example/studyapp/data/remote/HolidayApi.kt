package com.example.studyapp.data.remote

import com.example.studyapp.data.model.Holiday
import retrofit2.http.GET
import retrofit2.http.Path

interface HolidayApi {

    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): List<Holiday>
}