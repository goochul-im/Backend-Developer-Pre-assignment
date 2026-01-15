package com.assignment.feedback.application.port.out

import com.assignment.feedback.domain.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FeedbackRepository {
    fun save(feedback: Feedback): Feedback
    fun findById(id: Long): Feedback?
    fun findByUserIdAndChatId(userId: Long, chatId: Long): Feedback?
    fun findByUserId(userId: Long, isPositive: Boolean?, pageable: Pageable): Page<Feedback>
    fun findAll(isPositive: Boolean?, pageable: Pageable): Page<Feedback>
}
