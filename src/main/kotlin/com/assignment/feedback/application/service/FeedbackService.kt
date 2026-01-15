package com.assignment.feedback.application.service

import com.assignment.chat.application.port.out.ChatRepository
import com.assignment.common.exception.BusinessException
import com.assignment.common.exception.ErrorCode
import com.assignment.feedback.application.port.`in`.CreateFeedbackUseCase
import com.assignment.feedback.application.port.`in`.QueryFeedbacksUseCase
import com.assignment.feedback.application.port.`in`.UpdateFeedbackStatusUseCase
import com.assignment.feedback.application.port.out.FeedbackRepository
import com.assignment.feedback.domain.Feedback
import com.assignment.thread.application.port.out.ChatThreadRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional(readOnly = true)
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository,
    private val chatThreadRepository: ChatThreadRepository
) : CreateFeedbackUseCase, QueryFeedbacksUseCase, UpdateFeedbackStatusUseCase {

    @Transactional
    override fun createFeedback(command: CreateFeedbackUseCase.CreateFeedbackCommand): Feedback {
        val chat = chatRepository.findById(command.chatId)
            ?: throw BusinessException(ErrorCode.CHAT_NOT_FOUND)

        val thread = chatThreadRepository.findById(chat.threadId)
            ?: throw BusinessException(ErrorCode.THREAD_NOT_FOUND)

        if (!command.isAdmin && thread.userId != command.userId) {
            throw BusinessException(ErrorCode.FEEDBACK_ACCESS_DENIED)
        }

        feedbackRepository.findByUserIdAndChatId(command.userId, command.chatId)?.let {
            throw BusinessException(ErrorCode.FEEDBACK_ALREADY_EXISTS)
        }

        val feedback = Feedback(
            userId = command.userId,
            chatId = command.chatId,
            isPositive = command.isPositive,
            createdAt = OffsetDateTime.now()
        )

        return feedbackRepository.save(feedback)
    }

    override fun getFeedbacksByUser(
        userId: Long,
        isPositive: Boolean?,
        pageable: Pageable
    ): Page<Feedback> {
        return feedbackRepository.findByUserId(userId, isPositive, pageable)
    }

    override fun getAllFeedbacks(isPositive: Boolean?, pageable: Pageable): Page<Feedback> {
        return feedbackRepository.findAll(isPositive, pageable)
    }

    @Transactional
    override fun updateStatus(command: UpdateFeedbackStatusUseCase.UpdateFeedbackStatusCommand): Feedback {
        val feedback = feedbackRepository.findById(command.feedbackId)
            ?: throw BusinessException(ErrorCode.FEEDBACK_NOT_FOUND)

        val updatedFeedback = feedback.updateStatus(command.status)
        return feedbackRepository.save(updatedFeedback)
    }
}
