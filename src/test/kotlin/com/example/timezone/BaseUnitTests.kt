package com.example.timezone

import org.junit.jupiter.api.BeforeEach
import java.util.TimeZone

abstract class BaseUnitTests {
    @BeforeEach
    fun init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }
}