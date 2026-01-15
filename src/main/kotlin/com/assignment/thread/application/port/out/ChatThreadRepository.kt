package com.assignment.thread.application.port.out

import com.assignment.thread.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.OffsetDateTime

interface ChatThreadRepository {
    fun save(chatThread: ChatThread): ChatThread
    fun findById(id: Long): ChatThread?
    fun findByUserId(userId: Long, pageable: Pageable): Page<ChatThread>
    fun findAll(pageable: Pageable): Page<ChatThread>
    fun findLatestByUserId(userId: Long): ChatThread?
    fun updateLastActivityAt(id: Long, lastActivityAt: OffsetDateTime)
    fun deleteById(id: Long)
    fun existsById(id: Long): Boolean
}
