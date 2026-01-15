package com.assignment.chat.adapter.out.persistence

import com.assignment.chat.application.port.out.ChatRepository
import com.assignment.chat.domain.Chat
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class ChatRepositoryAdapter(
    private val chatJpaRepository: ChatJpaRepository
) : ChatRepository {

    override fun save(chat: Chat): Chat {
        val entity = ChatJpaEntity.from(chat)
        val savedEntity = chatJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: Long): Chat? {
        return chatJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByThreadId(threadId: Long): List<Chat> {
        return chatJpaRepository.findByThreadId(threadId)
            .map { it.toDomain() }
    }

    override fun findByThreadIds(threadIds: List<Long>): List<Chat> {
        return chatJpaRepository.findByThreadIdIn(threadIds)
            .map { it.toDomain() }
    }

    override fun deleteByThreadId(threadId: Long) {
        chatJpaRepository.deleteByThreadId(threadId)
    }

    override fun countByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long {
        return chatJpaRepository.countByCreatedAtBetween(start, end)
    }

    override fun findByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): List<Chat> {
        return chatJpaRepository.findByCreatedAtBetween(start, end)
            .map { it.toDomain() }
    }
}
