package com.assignment.user.application.port.`in`

interface LoginUseCase {
    fun login(command: LoginCommand): LoginResult

    data class LoginCommand(
        val email: String,
        val password: String
    )

    data class LoginResult(
        val accessToken: String,
        val expiresIn: Long
    )
}
