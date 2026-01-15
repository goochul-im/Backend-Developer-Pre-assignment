package com.assignment.user.adapter.`in`.web

import com.assignment.common.infrastructure.security.CustomUserDetails
import com.assignment.common.response.ApiResponse
import com.assignment.user.adapter.`in`.web.dto.LoginRequest
import com.assignment.user.adapter.`in`.web.dto.LoginResponse
import com.assignment.user.adapter.`in`.web.dto.SignUpRequest
import com.assignment.user.adapter.`in`.web.dto.UserResponse
import com.assignment.user.application.port.`in`.GetUserUseCase
import com.assignment.user.application.port.`in`.LoginUseCase
import com.assignment.user.application.port.`in`.SignUpUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val getUserUseCase: GetUserUseCase
) {

    @PostMapping("/auth/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<UserResponse>> {
        val user = signUpUseCase.signUp(request.toCommand())
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(UserResponse.from(user)))
    }

    @PostMapping("/auth/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val result = loginUseCase.login(request.toCommand())
        return ResponseEntity.ok(ApiResponse.success(LoginResponse.from(result)))
    }

    @GetMapping("/users/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val user = getUserUseCase.getUserByEmail(userDetails.username)
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)))
    }
}
