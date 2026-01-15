package com.assignment.user.application.port.`in`

import com.assignment.user.domain.User

interface GetUserUseCase {
    fun getUserByEmail(email: String): User
}
