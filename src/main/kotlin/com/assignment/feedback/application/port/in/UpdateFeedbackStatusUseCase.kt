package com.assignment.feedback.application.port.`in`

import com.assignment.feedback.domain.Feedback
import com.assignment.feedback.domain.FeedbackStatus

interface UpdateFeedbackStatusUseCase {
    fun updateStatus(command: UpdateFeedbackStatusCommand): Feedback

    data class UpdateFeedbackStatusCommand(
        val feedbackId: Long,
        val status: FeedbackStatus
    )
}
