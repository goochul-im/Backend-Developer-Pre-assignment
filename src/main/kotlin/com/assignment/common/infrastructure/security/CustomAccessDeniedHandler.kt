package com.assignment.common.infrastructure.security

import com.assignment.common.infrastructure.security.dto.SecurityErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: org.springframework.security.access.AccessDeniedException?
    ) {
        val httpStatus = HttpStatus.FORBIDDEN

        response.status = httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = SecurityErrorResponse(
            status = httpStatus.value(),
            error = httpStatus.reasonPhrase,
            message = "접근 권한이 없습니다."
        )

        objectMapper.writeValue(response.writer, errorResponse)
    }

}
