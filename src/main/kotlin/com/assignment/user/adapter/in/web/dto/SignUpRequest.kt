package com.assignment.user.adapter.`in`.web.dto

import com.assignment.user.application.port.`in`.SignUpUseCase
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    val password: String,

    @field:NotBlank(message = "이름은 필수입니다")
    @field:Size(min = 2, max = 100, message = "이름은 2자 이상 100자 이하여야 합니다")
    val name: String
) {
    fun toCommand(): SignUpUseCase.SignUpCommand = SignUpUseCase.SignUpCommand(
        email = email,
        password = password,
        name = name
    )
}
