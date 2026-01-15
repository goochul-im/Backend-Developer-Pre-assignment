package com.assignment.common.infrastructure.security

import com.assignment.common.infrastructure.security.dto.SecurityErrorResponse
import com.assignment.common.response.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val httpStatus = HttpStatus.UNAUTHORIZED

        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = SecurityErrorResponse(
            status = httpStatus.value(),
            error = httpStatus.reasonPhrase,
            message = "인증에 실패했습니다. (로그인이 필요합니다)"
        )

        objectMapper.writeValue(response.writer, errorResponse)
    }
}
