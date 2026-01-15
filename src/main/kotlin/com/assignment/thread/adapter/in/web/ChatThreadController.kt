package com.assignment.thread.adapter.`in`.web

import com.assignment.common.infrastructure.security.CustomUserDetails
import com.assignment.common.response.ApiResponse
import com.assignment.thread.application.port.`in`.DeleteChatThreadUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/threads")
class ChatThreadController(
    private val deleteChatThreadUseCase: DeleteChatThreadUseCase
) {

    @DeleteMapping("/{threadId}")
    fun deleteChatThread(
        @PathVariable threadId: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<Unit>> {
        deleteChatThreadUseCase.deleteChatThread(
            DeleteChatThreadUseCase.DeleteChatThreadCommand(
                threadId = threadId,
                requesterId = userDetails.getUserId()
            )
        )
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}
