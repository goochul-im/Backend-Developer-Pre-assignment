package com.assignment.user.domain

import java.time.OffsetDateTime

data class User(
    val id: Long = 0,
    val email: String,
    val password: String,
    val name: String,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    val role: Role = Role.MEMBER
)
