package com.example.timezone

import org.h2.tools.Server
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import javax.persistence.EntityManager
import javax.sql.DataSource

@SpringBootTest
abstract class BaseIntegrationTests {

    class KMySQLContainer(image: String) : MySQLContainer<KMySQLContainer>(image)

    companion object {
        private val TZ_Los_Angeles = "America/Los_Angeles"
        private val TZ_UTC = "UTC"

        private val mysqlServerTimezone = TZ_Los_Angeles
        private val mysqlConnectionTimezone = TZ_Los_Angeles
        private val forceConnectionTimeZoneToSession = "false"

        val mySQLContainer = KMySQLContainer("mysql:5.7.33")
            .withDatabaseName("mydb")
            .withUsername("myuser")
            .withPassword("mypass")
            .withReuse(true)
            .withEnv("TZ", mysqlServerTimezone)
            .withUrlParam("serverTimezone", mysqlConnectionTimezone) //connectionTimezone
            .withUrlParam("forceConnectionTimeZoneToSession", forceConnectionTimeZoneToSession)

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", mySQLContainer::getUsername)
            registry.add("spring.datasource.password", mySQLContainer::getPassword)
        }
    }

    init {
        mySQLContainer.start()
    }

    @Autowired(required = false)
    protected lateinit var dataSource: DataSource

    @Autowired(required = false)
    protected lateinit var entityManager: EntityManager

    @Autowired(required = false)
    protected lateinit var jdbcTemplate: JdbcTemplate

    fun openDBConsole() {
        Server.startWebServer(DataSourceUtils.getConnection(dataSource))
    }

    fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }
}