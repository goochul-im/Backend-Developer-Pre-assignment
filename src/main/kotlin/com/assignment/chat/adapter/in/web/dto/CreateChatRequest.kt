package com.assignment.chat.adapter.`in`.web.dto

import com.assignment.chat.application.port.`in`.CreateChatUseCase
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateChatRequest(
    @field:NotBlank(message = "질문은 필수입니다")
    @field:Size(min = 1, max = 10000, message = "질문은 1자 이상 10000자 이하여야 합니다")
    val question: String,

    val model: String? = null,

    val isStreaming: Boolean = false
) {
    fun toCommand(userId: Long): CreateChatUseCase.CreateChatCommand =
        CreateChatUseCase.CreateChatCommand(
            userId = userId,
            question = question,
            model = model,
            isStreaming = isStreaming
        )
}
