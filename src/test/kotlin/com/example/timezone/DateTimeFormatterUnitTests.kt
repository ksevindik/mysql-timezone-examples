package com.example.timezone

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date


class DateTimeFormatterUnitTests : BaseUnitTests() {
    @Test
    fun `local date time values should be built with timezone specified and formatted accordingly`() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val date = Date(1661238000000)
        val localDateInUTC = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"))
        val localDateInPDT = LocalDateTime.ofInstant(date.toInstant(),ZoneId.of("America/Los_Angeles"))
        Assertions.assertEquals("2022-08-23T07:00:00.000",localDateInUTC.format(formatter))
        Assertions.assertEquals("2022-08-23T00:00:00.000",localDateInPDT.format(formatter))
    }
}