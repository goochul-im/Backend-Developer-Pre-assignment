package com.assignment.chat.adapter.`in`.web

import com.assignment.chat.adapter.`in`.web.dto.ChatResponse
import com.assignment.chat.adapter.`in`.web.dto.ChatThreadWithChatsResponse
import com.assignment.chat.adapter.`in`.web.dto.CreateChatRequest
import com.assignment.chat.application.port.`in`.CreateChatUseCase
import com.assignment.chat.application.port.`in`.QueryChatsUseCase
import com.assignment.common.infrastructure.security.CustomUserDetails
import com.assignment.common.response.ApiResponse
import com.assignment.user.domain.Role
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val createChatUseCase: CreateChatUseCase,
    private val queryChatsUseCase: QueryChatsUseCase
) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody request: CreateChatRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val chat = createChatUseCase.createChat(request.toCommand(userDetails.getUserId()))
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(ChatResponse.from(chat)))
    }

    @GetMapping
    fun getChats(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PageableDefault(size = 20, sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ChatThreadWithChatsResponse>>> {
        val result = if (userDetails.getRole() == Role.ADMIN) {
            queryChatsUseCase.getAllChats(pageable)
        } else {
            queryChatsUseCase.getChatsByUser(userDetails.getUserId(), pageable)
        }

        val response = result.map { ChatThreadWithChatsResponse.from(it) }
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
