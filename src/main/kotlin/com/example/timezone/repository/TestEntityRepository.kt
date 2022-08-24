package com.example.timezone.repository

import com.example.timezone.model.TestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TestEntityRepository : JpaRepository<TestEntity,Long> {
}