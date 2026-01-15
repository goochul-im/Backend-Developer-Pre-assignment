package com.assignment.user.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime

interface LoginHistoryJpaRepository : JpaRepository<LoginHistoryJpaEntity, Long> {
    fun countByLoginAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long
}
