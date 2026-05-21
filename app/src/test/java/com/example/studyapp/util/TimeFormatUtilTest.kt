package com.example.studyapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeFormatUtilTest {

    @Test
    fun normalizeTimeInput_acceptsCommonTimeFormats() {
        assertEquals("07:00", normalizeTimeInput("7"))
        assertEquals("07:00", normalizeTimeInput("700"))
        assertEquals("07:30", normalizeTimeInput("730"))
        assertEquals("07:30", normalizeTimeInput("0730"))
        assertEquals("07:30", normalizeTimeInput("7:30"))
        assertEquals("07:30", normalizeTimeInput("07:30"))
        assertEquals("00:00", normalizeTimeInput("24"))
        assertEquals("00:00", normalizeTimeInput("2400"))
        assertEquals("00:00", normalizeTimeInput("24:00"))
        assertEquals("07:30", normalizeTimeInput("7시30분"))
    }

    @Test
    fun normalizeTimeInput_rejectsInvalidTimes() {
        assertNull(normalizeTimeInput(""))
        assertNull(normalizeTimeInput("24:01"))
        assertNull(normalizeTimeInput("12:60"))
        assertNull(normalizeTimeInput("1260"))
        assertNull(normalizeTimeInput("12345"))
    }
}
