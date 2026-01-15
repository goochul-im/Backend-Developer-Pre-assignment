package com.assignment.user.domain

import java.time.OffsetDateTime

data class LoginHistory(
    val id: Long? = null,
    val userId: Long,
    val loginAt: OffsetDateTime = OffsetDateTime.now()
)
