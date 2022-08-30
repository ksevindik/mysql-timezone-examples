package com.example.timezone

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date
import java.util.TimeZone

class SimpleDateFormatUnitTests : BaseUnitTests() {
    @Test
    fun `simple date format timezone property should be used during parsing UNLESS offset is given`() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss")
        val dateString = "2022-08-23T07:00:00.000"

        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        val dateParsedWithUTC = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithUTC.time,Matchers.equalTo(1661238000000))

        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        val dateParsedWithPDT = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithPDT.time,Matchers.equalTo(1661263200000))
    }

    @Test
    fun `simple date format timezone property should NOT be used during parsing IF offset is given`() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ")
        val dateString = "2022-08-23T07:00:00.000+0000"

        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        val dateParsedWithUTC = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithUTC.time,Matchers.equalTo(1661238000000))

        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        val dateParsedWithPDT = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithPDT.time,Matchers.equalTo(1661238000000))
    }

    @Test
    fun `simple date format timezone property should be used during formatting UNLESS offset is given`() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss")
        val date1 = Date(1661238000000)
        val date2 = Date(1661263200000)

        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T07:00:00.000"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T14:00:00.000"))

        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T00:00:00.000"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T07:00:00.000"))
    }

    @Test
    fun `simple date format timezone property should NOT be used during formatting IF offset is given`() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ")
        val date1 = Date(1661238000000)
        val date2 = Date(1661263200000)

        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T07:00:00.000+0000"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T14:00:00.000+0000"))

        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T00:00:00.000-0700"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T07:00:00.000-0700"))
    }
}