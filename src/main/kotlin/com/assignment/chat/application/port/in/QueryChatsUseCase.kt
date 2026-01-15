package com.assignment.chat.application.port.`in`

import com.assignment.chat.domain.Chat
import com.assignment.thread.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QueryChatsUseCase {
    fun getChatsByUser(userId: Long, pageable: Pageable): Page<ChatThreadWithChats>
    fun getAllChats(pageable: Pageable): Page<ChatThreadWithChats>

    data class ChatThreadWithChats(
        val thread: ChatThread,
        val chats: List<Chat>
    )
}
