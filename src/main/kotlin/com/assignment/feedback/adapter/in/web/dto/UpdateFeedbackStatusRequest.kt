package com.assignment.feedback.adapter.`in`.web.dto

import com.assignment.feedback.application.port.`in`.UpdateFeedbackStatusUseCase
import com.assignment.feedback.domain.FeedbackStatus
import jakarta.validation.constraints.NotNull

data class UpdateFeedbackStatusRequest(
    @field:NotNull(message = "상태는 필수입니다")
    val status: FeedbackStatus
) {
    fun toCommand(feedbackId: Long): UpdateFeedbackStatusUseCase.UpdateFeedbackStatusCommand {
        return UpdateFeedbackStatusUseCase.UpdateFeedbackStatusCommand(
            feedbackId = feedbackId,
            status = status
        )
    }
}
