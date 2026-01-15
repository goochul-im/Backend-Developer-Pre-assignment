package com.assignment.user.adapter.out.persistence

import com.assignment.user.application.port.out.UserRepository
import com.assignment.user.domain.User
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User {
        val entity = UserJpaEntity.from(user)
        val savedEntity = userJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun findByEmail(email: String): User? {
        return userJpaRepository.findByEmail(email)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findById(id: Long): User? {
        return userJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun existsByEmail(email: String): Boolean {
        return userJpaRepository.existsByEmail(email)
    }

    override fun countByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long {
        return userJpaRepository.countByCreatedAtBetween(start, end)
    }
}
