package com.assignment.feedback.application.port.`in`

import com.assignment.feedback.domain.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QueryFeedbacksUseCase {
    fun getFeedbacksByUser(userId: Long, isPositive: Boolean?, pageable: Pageable): Page<Feedback>
    fun getAllFeedbacks(isPositive: Boolean?, pageable: Pageable): Page<Feedback>
}
