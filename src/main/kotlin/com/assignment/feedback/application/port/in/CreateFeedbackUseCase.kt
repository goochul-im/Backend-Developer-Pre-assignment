package com.assignment.feedback.application.port.`in`

import com.assignment.feedback.domain.Feedback

interface CreateFeedbackUseCase {
    fun createFeedback(command: CreateFeedbackCommand): Feedback

    data class CreateFeedbackCommand(
        val userId: Long,
        val chatId: Long,
        val isPositive: Boolean,
        val isAdmin: Boolean = false
    )
}
