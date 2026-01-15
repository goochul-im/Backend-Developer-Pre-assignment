package com.assignment.thread.adapter.out.persistence

import com.assignment.thread.application.port.out.ChatThreadRepository
import com.assignment.thread.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class ChatThreadRepositoryAdapter(
    private val chatThreadJpaRepository: ChatThreadJpaRepository
) : ChatThreadRepository {

    override fun save(chatThread: ChatThread): ChatThread {
        val entity = ChatThreadJpaEntity.from(chatThread)
        val savedEntity = chatThreadJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: Long): ChatThread? {
        return chatThreadJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserId(userId: Long, pageable: Pageable): Page<ChatThread> {
        return chatThreadJpaRepository.findByUserId(userId, pageable)
            .map { it.toDomain() }
    }

    override fun findAll(pageable: Pageable): Page<ChatThread> {
        return chatThreadJpaRepository.findAll(pageable)
            .map { it.toDomain() }
    }

    override fun findLatestByUserId(userId: Long): ChatThread? {
        return chatThreadJpaRepository.findTopByUserIdOrderByLastActivityAtDesc(userId)
            ?.toDomain()
    }

    override fun updateLastActivityAt(id: Long, lastActivityAt: OffsetDateTime) {
        chatThreadJpaRepository.updateLastActivityAt(id, lastActivityAt)
    }

    override fun deleteById(id: Long) {
        chatThreadJpaRepository.deleteById(id)
    }

    override fun existsById(id: Long): Boolean {
        return chatThreadJpaRepository.existsById(id)
    }
}
