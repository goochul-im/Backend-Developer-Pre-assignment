package com.assignment.user.application.port.out

import com.assignment.user.domain.User
import java.time.OffsetDateTime

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun findById(id: Long): User?
    fun existsByEmail(email: String): Boolean
    fun countByCreatedAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long
}
