package com.assignment.thread.application.port.`in`

interface DeleteChatThreadUseCase {
    fun deleteChatThread(command: DeleteChatThreadCommand)

    data class DeleteChatThreadCommand(
        val threadId: Long,
        val requesterId: Long
    )
}
