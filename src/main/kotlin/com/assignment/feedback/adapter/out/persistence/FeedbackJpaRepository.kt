package com.assignment.feedback.adapter.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FeedbackJpaRepository : JpaRepository<FeedbackJpaEntity, Long> {
    fun findByUserIdAndChatId(userId: Long, chatId: Long): FeedbackJpaEntity?

    fun findByUserId(userId: Long, pageable: Pageable): Page<FeedbackJpaEntity>

    @Query("SELECT f FROM FeedbackJpaEntity f WHERE f.userId = :userId AND f.isPositive = :isPositive")
    fun findByUserIdAndIsPositive(userId: Long, isPositive: Boolean, pageable: Pageable): Page<FeedbackJpaEntity>

    @Query("SELECT f FROM FeedbackJpaEntity f WHERE f.isPositive = :isPositive")
    fun findAllByIsPositive(isPositive: Boolean, pageable: Pageable): Page<FeedbackJpaEntity>
}
