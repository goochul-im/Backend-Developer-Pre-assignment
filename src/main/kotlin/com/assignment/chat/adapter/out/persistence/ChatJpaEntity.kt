package com.assignment.chat.adapter.out.persistence

import com.assignment.chat.domain.Chat
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "chats")
class ChatJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "thread_id", nullable = false)
    val threadId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val answer: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun toDomain(): Chat = Chat(
        id = id,
        threadId = threadId,
        question = question,
        answer = answer,
        createdAt = createdAt
    )

    companion object {
        fun from(chat: Chat): ChatJpaEntity = ChatJpaEntity(
            id = chat.id,
            threadId = chat.threadId,
            question = chat.question,
            answer = chat.answer,
            createdAt = chat.createdAt
        )
    }
}
