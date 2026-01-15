package com.assignment.chat.application.service

import com.assignment.chat.application.port.`in`.CreateChatUseCase
import com.assignment.chat.application.port.`in`.QueryChatsUseCase
import com.assignment.chat.application.port.out.AiClient
import com.assignment.chat.application.port.out.ChatRepository
import com.assignment.chat.domain.Chat
import com.assignment.thread.application.port.out.ChatThreadRepository
import com.assignment.thread.domain.ChatThread
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
@Transactional(readOnly = true)
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatThreadRepository: ChatThreadRepository,
    private val aiClient: AiClient
) : CreateChatUseCase, QueryChatsUseCase {

    @Transactional
    override fun createChat(command: CreateChatUseCase.CreateChatCommand): Chat {
        val now = OffsetDateTime.now()

        // 스레드 결정: 기존 스레드 사용 또는 새 스레드 생성
        val thread = resolveThread(command.userId, now)

        // 대화 이력 조회 (AI 컨텍스트용)
        val conversationHistory = chatRepository.findByThreadId(thread.id!!)

        // AI 응답 생성
        val aiResponse = aiClient.generateAnswer(
            question = command.question,
            conversationHistory = conversationHistory,
            model = command.model,
            isStreaming = command.isStreaming
        )

        // 채팅 저장
        val chat = Chat(
            threadId = thread.id,
            question = command.question,
            answer = aiResponse.answer,
            createdAt = now
        )

        return chatRepository.save(chat)
    }

    private fun resolveThread(userId: Long, now: OffsetDateTime): ChatThread {
        val lastThread = chatThreadRepository.findLatestByUserId(userId)

        return if (lastThread == null || lastThread.isExpired(now)) {
            // 새 스레드 생성
            chatThreadRepository.save(
                ChatThread(
                    userId = userId,
                    lastActivityAt = now,
                    createdAt = now
                )
            )
        } else {
            // 기존 스레드 사용, lastActivityAt 업데이트
            chatThreadRepository.updateLastActivityAt(lastThread.id!!, now)
            lastThread.updateLastActivityAt(now)
            lastThread
        }
    }

    override fun getChatsByUser(userId: Long, pageable: Pageable): Page<QueryChatsUseCase.ChatThreadWithChats> {
        val threadsPage = chatThreadRepository.findByUserId(userId, pageable)
        return mapThreadsToChatsPage(threadsPage, pageable)
    }

    override fun getAllChats(pageable: Pageable): Page<QueryChatsUseCase.ChatThreadWithChats> {
        val threadsPage = chatThreadRepository.findAll(pageable)
        return mapThreadsToChatsPage(threadsPage, pageable)
    }

    private fun mapThreadsToChatsPage(
        threadsPage: Page<ChatThread>,
        pageable: Pageable
    ): Page<QueryChatsUseCase.ChatThreadWithChats> {
        val threadIds = threadsPage.content.map { it.id!! }
        val allChats = if (threadIds.isNotEmpty()) {
            chatRepository.findByThreadIds(threadIds)
        } else {
            emptyList()
        }

        val chatsByThreadId = allChats.groupBy { it.threadId }

        val content = threadsPage.content.map { thread ->
            QueryChatsUseCase.ChatThreadWithChats(
                thread = thread,
                chats = chatsByThreadId[thread.id] ?: emptyList()
            )
        }

        return PageImpl(content, pageable, threadsPage.totalElements)
    }
}
