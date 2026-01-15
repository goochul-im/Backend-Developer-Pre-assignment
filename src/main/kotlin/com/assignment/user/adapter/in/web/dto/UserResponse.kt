package com.assignment.user.adapter.`in`.web.dto

import com.assignment.user.domain.Role
import com.assignment.user.domain.User
import java.time.OffsetDateTime

data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: Role,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(user: User): UserResponse = UserResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            role = user.role,
            createdAt = user.createdAt
        )
    }
}
