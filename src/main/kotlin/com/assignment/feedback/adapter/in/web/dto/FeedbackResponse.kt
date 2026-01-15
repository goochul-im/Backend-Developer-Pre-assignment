package com.assignment.feedback.adapter.`in`.web.dto

import com.assignment.feedback.domain.Feedback
import com.assignment.feedback.domain.FeedbackStatus
import java.time.OffsetDateTime

data class FeedbackResponse(
    val id: Long,
    val userId: Long,
    val chatId: Long,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(feedback: Feedback): FeedbackResponse {
            return FeedbackResponse(
                id = feedback.id!!,
                userId = feedback.userId,
                chatId = feedback.chatId,
                isPositive = feedback.isPositive,
                status = feedback.status,
                createdAt = feedback.createdAt
            )
        }
    }
}
