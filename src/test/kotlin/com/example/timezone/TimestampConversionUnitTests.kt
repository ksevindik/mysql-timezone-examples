package com.example.timezone

import com.google.protobuf.Timestamp
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Date

class TimestampConversionUnitTests : BaseUnitTests() {
    @Test
    fun `it should obtain protobuf timestamp from java date object and vice versa`() {
        val now = Date()
        val millis = now.time
        val ts = Timestamp.newBuilder()
                .setSeconds(millis/1000)
                .setNanos(((millis % 1000) * 1_000_000).toInt()).build()

        val now2 = Date(ts.seconds * 1000 + ts.nanos / 1_000_000)

        Assertions.assertEquals(millis,now2.time)
    }
}