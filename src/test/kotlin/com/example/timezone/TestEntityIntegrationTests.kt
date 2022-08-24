package com.example.timezone

import com.example.timezone.model.TestEntity
import com.example.timezone.repository.TestEntityRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.TimeZone

@Transactional
class TestEntityIntegrationTests : BaseIntegrationTests() {
    /*
select @@system_time_zone, @@global.time_zone, @@session.time_zone;

select * from test_entity;

set  @@session.time_zone = 'UTC';

select @@system_time_zone, @@global.time_zone, @@session.time_zone;

select * from test_entity;
 */

    @Autowired
    private lateinit var testEntityRepository: TestEntityRepository

    @Test
    fun `run console`() {
        openDBConsole()
    }

    @Test
    fun `ts values stored while losing their ordering precision when pdt daylight saving time moves one hour backward`() {
        /*
        private val mysqlServerTimezone = TZ_Los_Angeles
        private val mysqlConnectionTimezone = TZ_Los_Angeles
        private val forceConnectionTimeZoneToSession = "false"
         */
        Arrays.asList(
            "2022-11-06T08:59:00.000",
            "2022-11-06T09:01:00.000",
            "2022-11-06T09:59:00.000",
            "2022-11-06T10:01:00.000").forEach {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").parse(it)
            val testEntity = TestEntity()
            testEntity.tsValue = date
            testEntity.dtValue = date
            testEntityRepository.save(testEntity)
        }
        flushAndClear()
        openDBConsole()
    }

    @Test
    fun `ts values should be stored properly when pdt daylight saving time moves one hour ahead`() {
        /*
        private val mysqlServerTimezone = TZ_Los_Angeles
        private val mysqlConnectionTimezone = TZ_Los_Angeles
        private val forceConnectionTimeZoneToSession = "false"
         */
        Arrays.asList(
            "2022-03-13T09:59:00.000",
            "2022-03-13T10:01:00.000").forEach {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss").parse(it)
            val testEntity = TestEntity()
            testEntity.tsValue = date
            testEntity.dtValue = date
            testEntityRepository.save(testEntity)
        }
        flushAndClear()
        openDBConsole()
    }

    @Test
    fun `invalid date value problem occurs during insert when connection timezone is given as UTC but mysql server timezone is PDT`() {
        /*
        private val mysqlServerTimezone = TZ_Los_Angeles
        private val mysqlConnectionTimezone = TZ_UTC
        private val forceConnectionTimeZoneToSession = "false"
        should be as above to get this error
         */
        val date = Date(1647136860000) //2022-03-13T02:01:00.000 in UTC
        val testEntity = TestEntity()
        testEntity.tsValue = date
        testEntity.dtValue = date
        val ex = Assertions.assertThrows(DataIntegrityViolationException::class.java) {
            testEntityRepository.save(testEntity)
        }
        ex.printStackTrace()
        flushAndClear()
        //openDBConsole()
    }

    @Test
    fun `ts and dt values should be same as the values fetched from DB`() {
        val date = Date(1661238000000) //2022-08-23T07:00:00.000
        val testEntity = TestEntity()
        testEntity.tsValue = date
        testEntity.dtValue = date
        testEntityRepository.save(testEntity)
        flushAndClear()
        //openDBConsole()
        Assertions.assertEquals("UTC",TimeZone.getDefault().id)
        val testEntityFetchedFromDB = testEntityRepository.findById(testEntity.id!!).orElse(null)
        Assertions.assertEquals(testEntity.tsValue?.time,testEntityFetchedFromDB?.tsValue?.time)
        Assertions.assertEquals(testEntity.dtValue?.time,testEntityFetchedFromDB?.dtValue?.time)
    }
}