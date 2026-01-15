package com.assignment.chat.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime

interface ChatJpaRepository : JpaRepository<ChatJpaEntity, Long> {
    fun findByThreadId(threadId: Long): List<ChatJpaEntity>
    fun findByThreadIdIn(threadIds: List<Long>): List<ChatJpaEntity>
    fun deleteByThreadId(threadId: Long)
    fun countByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long
    fun findByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): List<ChatJpaEntity>
}
