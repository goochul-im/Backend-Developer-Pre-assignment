package com.assignment.thread.application.service

import com.assignment.common.exception.BusinessException
import com.assignment.common.exception.ErrorCode
import com.assignment.thread.application.port.`in`.DeleteChatThreadUseCase
import com.assignment.thread.application.port.`in`.QueryChatThreadsUseCase
import com.assignment.thread.application.port.out.ChatThreadRepository
import com.assignment.thread.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatThreadService(
    private val chatThreadRepository: ChatThreadRepository
) : QueryChatThreadsUseCase, DeleteChatThreadUseCase {

    override fun getChatThreadsByUser(userId: Long, pageable: Pageable): Page<ChatThread> {
        return chatThreadRepository.findByUserId(userId, pageable)
    }

    override fun getAllChatThreads(pageable: Pageable): Page<ChatThread> {
        return chatThreadRepository.findAll(pageable)
    }

    override fun getChatThreadById(threadId: Long): ChatThread {
        return chatThreadRepository.findById(threadId)
            ?: throw BusinessException(ErrorCode.THREAD_NOT_FOUND)
    }

    @Transactional
    override fun deleteChatThread(command: DeleteChatThreadUseCase.DeleteChatThreadCommand) {
        val chatThread = chatThreadRepository.findById(command.threadId)
            ?: throw BusinessException(ErrorCode.THREAD_NOT_FOUND)

        if (chatThread.userId != command.requesterId) {
            throw BusinessException(ErrorCode.THREAD_ACCESS_DENIED)
        }

        chatThreadRepository.deleteById(command.threadId)
    }
}
