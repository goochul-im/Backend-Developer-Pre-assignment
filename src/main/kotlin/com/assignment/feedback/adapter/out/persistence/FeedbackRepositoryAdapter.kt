package com.assignment.feedback.adapter.out.persistence

import com.assignment.feedback.application.port.out.FeedbackRepository
import com.assignment.feedback.domain.Feedback
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class FeedbackRepositoryAdapter(
    private val feedbackJpaRepository: FeedbackJpaRepository
) : FeedbackRepository {

    override fun save(feedback: Feedback): Feedback {
        val entity = FeedbackJpaEntity.from(feedback)
        val savedEntity = feedbackJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findById(id: Long): Feedback? {
        return feedbackJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findByUserIdAndChatId(userId: Long, chatId: Long): Feedback? {
        return feedbackJpaRepository.findByUserIdAndChatId(userId, chatId)?.toDomain()
    }

    override fun findByUserId(userId: Long, isPositive: Boolean?, pageable: Pageable): Page<Feedback> {
        return if (isPositive != null) {
            feedbackJpaRepository.findByUserIdAndIsPositive(userId, isPositive, pageable)
        } else {
            feedbackJpaRepository.findByUserId(userId, pageable)
        }.map { it.toDomain() }
    }

    override fun findAll(isPositive: Boolean?, pageable: Pageable): Page<Feedback> {
        return if (isPositive != null) {
            feedbackJpaRepository.findAllByIsPositive(isPositive, pageable)
        } else {
            feedbackJpaRepository.findAll(pageable)
        }.map { it.toDomain() }
    }
}
