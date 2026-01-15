package com.assignment.feedback.adapter.`in`.web

import com.assignment.common.infrastructure.security.CustomUserDetails
import com.assignment.common.response.ApiResponse
import com.assignment.feedback.adapter.`in`.web.dto.CreateFeedbackRequest
import com.assignment.feedback.adapter.`in`.web.dto.FeedbackResponse
import com.assignment.feedback.adapter.`in`.web.dto.UpdateFeedbackStatusRequest
import com.assignment.feedback.application.port.`in`.CreateFeedbackUseCase
import com.assignment.feedback.application.port.`in`.QueryFeedbacksUseCase
import com.assignment.feedback.application.port.`in`.UpdateFeedbackStatusUseCase
import com.assignment.user.domain.Role
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feedbacks")
class FeedbackController(
    private val createFeedbackUseCase: CreateFeedbackUseCase,
    private val queryFeedbacksUseCase: QueryFeedbacksUseCase,
    private val updateFeedbackStatusUseCase: UpdateFeedbackStatusUseCase
) {

    @PostMapping
    fun createFeedback(
        @Valid @RequestBody request: CreateFeedbackRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<FeedbackResponse>> {
        val isAdmin = userDetails.getRole() == Role.ADMIN
        val feedback = createFeedbackUseCase.createFeedback(
            request.toCommand(userDetails.getUserId(), isAdmin)
        )
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(FeedbackResponse.from(feedback)))
    }

    @GetMapping
    fun getFeedbacks(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(required = false) isPositive: Boolean?,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<FeedbackResponse>>> {
        val result = if (userDetails.getRole() == Role.ADMIN) {
            queryFeedbacksUseCase.getAllFeedbacks(isPositive, pageable)
        } else {
            queryFeedbacksUseCase.getFeedbacksByUser(userDetails.getUserId(), isPositive, pageable)
        }

        val response = result.map { FeedbackResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PatchMapping("/{feedbackId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateFeedbackStatus(
        @PathVariable feedbackId: Long,
        @Valid @RequestBody request: UpdateFeedbackStatusRequest
    ): ResponseEntity<ApiResponse<FeedbackResponse>> {
        val feedback = updateFeedbackStatusUseCase.updateStatus(request.toCommand(feedbackId))
        return ResponseEntity.ok(ApiResponse.success(FeedbackResponse.from(feedback)))
    }
}
