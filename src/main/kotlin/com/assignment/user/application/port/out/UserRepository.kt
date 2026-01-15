package com.assignment.user.application.port.out

import com.assignment.user.domain.User

interface UserRepository {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
