package com.assignment.feedback.domain

import java.time.OffsetDateTime

class Feedback(
    val id: Long? = null,
    val userId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus = FeedbackStatus.PENDING,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun updateStatus(newStatus: FeedbackStatus): Feedback {
        return Feedback(
            id = this.id,
            userId = this.userId,
            chatId = this.chatId,
            isPositive = this.isPositive,
            status = newStatus,
            createdAt = this.createdAt
        )
    }
}

enum class FeedbackStatus {
    PENDING, RESOLVED
}
