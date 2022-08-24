package com.example.timezone

import com.example.timezone.model.TestEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.ZoneId
import java.util.Date
import java.util.TimeZone

@SpringBootTest
class JacksonObjectMapperIntegrationTests {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `objectMapper should convert using its own date format without a timezone set using UTC as default one independent of JVM default timezone`() {
        val date = Date(1661238000000) //2022-08-23T07:00:00.000
        val testEntity = TestEntity()
        testEntity.tsValue = date

        val actualJson = objectMapper.writeValueAsString(testEntity)

        val expectedJson = """{"id":null,"tsValue":"2022-08-23T07:00:00.000+00:00","dtValue":null}""".trimIndent()

        Assertions.assertEquals(expectedJson,actualJson)


        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))

        val actualJson2 = objectMapper.writeValueAsString(testEntity)
        val dateFormat = objectMapper.dateFormat

        val expectedJson2 = """{"id":null,"tsValue":"2022-08-23T07:00:00.000+00:00","dtValue":null}""".trimIndent()

        Assertions.assertEquals(expectedJson2,actualJson2)
    }
}