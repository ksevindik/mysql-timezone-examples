package com.example.timezone

import com.example.timezone.model.TestEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.util.Date
import java.util.TimeZone

@SpringBootTest
@TestPropertySource(properties = [
    "spring.jackson.timeZone=America/Los_Angeles",
])
class JacksonObjectMapperIntegrationTests2 {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `objectMapper should convert using its own date format using timezone given as property`() {
        val date = Date(1661238000000) //2022-08-23T07:00:00.000 in UTC
        val testEntity = TestEntity()
        testEntity.tsValue = date

        val actualJson = objectMapper.writeValueAsString(testEntity)

        val expectedJson = """{"id":null,"tsValue":"2022-08-23T00:00:00.000-07:00","dtValue":null}""".trimIndent()

        Assertions.assertEquals(expectedJson,actualJson)


        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val actualJson2 = objectMapper.writeValueAsString(testEntity)

        val expectedJson2 = """{"id":null,"tsValue":"2022-08-23T00:00:00.000-07:00","dtValue":null}""".trimIndent()

        Assertions.assertEquals(expectedJson2,actualJson2)
    }
}