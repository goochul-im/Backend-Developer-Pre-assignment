package com.assignment.thread.adapter.out.persistence

import com.assignment.thread.domain.ChatThread
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "chat_threads")
class ChatThreadJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "last_activity_at", nullable = false)
    var lastActivityAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun toDomain(): ChatThread = ChatThread(
        id = id,
        userId = userId,
        lastActivityAt = lastActivityAt,
        createdAt = createdAt
    )

    companion object {
        fun from(chatThread: ChatThread): ChatThreadJpaEntity = ChatThreadJpaEntity(
            id = chatThread.id,
            userId = chatThread.userId,
            lastActivityAt = chatThread.lastActivityAt,
            createdAt = chatThread.createdAt
        )
    }
}
