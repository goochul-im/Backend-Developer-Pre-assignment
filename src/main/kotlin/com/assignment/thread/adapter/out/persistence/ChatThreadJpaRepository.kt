package com.assignment.thread.adapter.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.Optional

interface ChatThreadJpaRepository : JpaRepository<ChatThreadJpaEntity, Long> {

    fun findByUserId(userId: Long, pageable: Pageable): Page<ChatThreadJpaEntity>

    @Query("SELECT t " +
            "FROM ChatThreadJpaEntity t " +
            "WHERE t.userId = :userId " +
            "ORDER BY t.lastActivityAt " +
            "DESC LIMIT 1")
    fun findTopByUserIdOrderByLastActivityAtDesc(@Param("userId") userId: Long): ChatThreadJpaEntity?

    @Modifying
    @Query("UPDATE ChatThreadJpaEntity t " +
            "SET t.lastActivityAt = :lastActivityAt " +
            "WHERE t.id = :id")
    fun updateLastActivityAt(@Param("id") id: Long, @Param("lastActivityAt") lastActivityAt: OffsetDateTime)

}
