package com.assignment.common.infrastructure.security.dto

data class SecurityErrorResponse(
    val status: Int,
    val error: String,
    val message: String
)
