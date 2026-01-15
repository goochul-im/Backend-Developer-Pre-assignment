package com.assignment.chat.adapter.`in`.web.dto

import com.assignment.chat.application.port.`in`.QueryChatsUseCase
import com.assignment.thread.adapter.`in`.web.dto.ChatThreadResponse

data class ChatThreadWithChatsResponse(
    val thread: ChatThreadResponse,
    val chats: List<ChatResponse>
) {
    companion object {
        fun from(data: QueryChatsUseCase.ChatThreadWithChats): ChatThreadWithChatsResponse =
            ChatThreadWithChatsResponse(
                thread = ChatThreadResponse.from(data.thread),
                chats = data.chats.map { ChatResponse.from(it) }
            )
    }
}
