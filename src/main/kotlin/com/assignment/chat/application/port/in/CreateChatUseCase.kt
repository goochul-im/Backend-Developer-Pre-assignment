package com.assignment.chat.application.port.`in`

import com.assignment.chat.domain.Chat
import reactor.core.publisher.Flux

interface CreateChatUseCase {
    fun createChat(command: CreateChatCommand): Chat

    data class CreateChatCommand(
        val userId: Long,
        val question: String,
        val model: String? = null,
        val isStreaming: Boolean = false
    )

}
