package com.assignment.user.adapter.`in`.web.dto

import com.assignment.user.application.port.`in`.LoginUseCase

data class LoginResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
) {
    companion object {
        fun from(result: LoginUseCase.LoginResult): LoginResponse = LoginResponse(
            accessToken = result.accessToken,
            expiresIn = result.expiresIn
        )
    }
}
