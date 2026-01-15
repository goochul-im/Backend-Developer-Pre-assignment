package com.assignment.user.application.port.`in`

import com.assignment.user.domain.User

interface SignUpUseCase {
    fun signUp(command: SignUpCommand): User

    data class SignUpCommand(
        val email: String,
        val password: String,
        val name: String
    )
}
