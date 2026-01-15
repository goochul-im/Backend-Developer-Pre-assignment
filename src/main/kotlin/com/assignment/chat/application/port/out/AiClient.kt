package com.assignment.chat.application.port.out

import com.assignment.chat.domain.Chat
import reactor.core.publisher.Flux

interface AiClient {
    fun generateAnswer(
        question: String,
        conversationHistory: List<Chat>,
        model: String?,
        isStreaming: Boolean
    ): AiResponse

    data class AiResponse(
        val answer: String,
        val isStreaming: Boolean = false
    )
}
