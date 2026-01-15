package com.assignment.feedback.adapter.out.persistence

import com.assignment.feedback.domain.Feedback
import com.assignment.feedback.domain.FeedbackStatus
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "chat_id"])
    ]
)
class FeedbackJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "chat_id", nullable = false)
    val chatId: Long,

    @Column(name = "is_positive", nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: FeedbackStatus = FeedbackStatus.PENDING,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun toDomain(): Feedback = Feedback(
        id = id,
        userId = userId,
        chatId = chatId,
        isPositive = isPositive,
        status = status,
        createdAt = createdAt
    )

    companion object {
        fun from(feedback: Feedback): FeedbackJpaEntity = FeedbackJpaEntity(
            id = feedback.id,
            userId = feedback.userId,
            chatId = feedback.chatId,
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt
        )
    }
}
