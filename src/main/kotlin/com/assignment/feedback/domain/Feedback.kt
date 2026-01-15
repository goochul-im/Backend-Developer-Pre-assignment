package com.assignment.feedback.domain

import java.time.OffsetDateTime

data class Feedback(
    val id: Long? = null,
    val userId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus = FeedbackStatus.PENDING,
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun updateStatus(newStatus: FeedbackStatus): Feedback = copy(status = newStatus)
}

enum class FeedbackStatus {
    PENDING, RESOLVED
}
