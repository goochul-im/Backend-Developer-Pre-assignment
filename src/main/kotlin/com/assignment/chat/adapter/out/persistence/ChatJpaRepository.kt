package com.assignment.chat.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ChatJpaRepository : JpaRepository<ChatJpaEntity, Long> {
    fun findByThreadId(threadId: Long): List<ChatJpaEntity>
    fun findByThreadIdIn(threadIds: List<Long>): List<ChatJpaEntity>
    fun deleteByThreadId(threadId: Long)
}
