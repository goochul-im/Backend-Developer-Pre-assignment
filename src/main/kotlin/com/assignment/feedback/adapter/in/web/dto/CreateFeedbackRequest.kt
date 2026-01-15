package com.assignment.feedback.adapter.`in`.web.dto

import com.assignment.feedback.application.port.`in`.CreateFeedbackUseCase
import jakarta.validation.constraints.NotNull

data class CreateFeedbackRequest(
    @field:NotNull(message = "대화 ID는 필수입니다")
    val chatId: Long,

    @field:NotNull(message = "긍정/부정 여부는 필수입니다")
    val isPositive: Boolean
) {
    fun toCommand(userId: Long, isAdmin: Boolean): CreateFeedbackUseCase.CreateFeedbackCommand {
        return CreateFeedbackUseCase.CreateFeedbackCommand(
            userId = userId,
            chatId = chatId,
            isPositive = isPositive,
            isAdmin = isAdmin
        )
    }
}
