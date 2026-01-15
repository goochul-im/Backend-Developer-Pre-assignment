package com.assignment.report.application.service

import com.assignment.chat.application.port.out.ChatRepository
import com.assignment.chat.domain.Chat
import com.assignment.thread.application.port.out.ChatThreadRepository
import com.assignment.thread.domain.ChatThread
import com.assignment.user.application.port.out.LoginHistoryRepository
import com.assignment.user.application.port.out.UserRepository
import com.assignment.user.domain.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class ReportServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var loginHistoryRepository: LoginHistoryRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var chatThreadRepository: ChatThreadRepository

    @InjectMocks
    private lateinit var reportService: ReportService

    @Nested
    @DisplayName("사용자 활동 기록 조회 테스트")
    inner class GetActivityStatsTest {

        @Test
        @DisplayName("성공: 하루 동안의 활동 기록 반환")
        fun getActivityStats_success() {
            // given
            whenever(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(5L)
            whenever(loginHistoryRepository.countByLoginAtBetween(any(), any())).thenReturn(10L)
            whenever(chatRepository.countByCreatedAtBetween(any(), any())).thenReturn(20L)

            // when
            val result = reportService.getActivityStats()

            // then
            assertEquals(5L, result.signUpCount)
            assertEquals(10L, result.loginCount)
            assertEquals(20L, result.chatCount)

            verify(userRepository).countByCreatedAtBetween(any(), any())
            verify(loginHistoryRepository).countByLoginAtBetween(any(), any())
            verify(chatRepository).countByCreatedAtBetween(any(), any())
        }

        @Test
        @DisplayName("성공: 데이터가 없을 때 0 반환")
        fun getActivityStats_noData_returnsZeros() {
            // given
            whenever(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L)
            whenever(loginHistoryRepository.countByLoginAtBetween(any(), any())).thenReturn(0L)
            whenever(chatRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L)

            // when
            val result = reportService.getActivityStats()

            // then
            assertEquals(0L, result.signUpCount)
            assertEquals(0L, result.loginCount)
            assertEquals(0L, result.chatCount)
        }
    }

    @Nested
    @DisplayName("CSV 보고서 생성 테스트")
    inner class GenerateChatReportTest {

        @Test
        @DisplayName("성공: 하루 동안의 대화 목록 CSV 생성")
        fun generateChatReport_success() {
            // given
            val now = OffsetDateTime.now()
            val userId = 1L
            val threadId = 100L

            val chats = listOf(
                Chat(
                    id = 1L,
                    threadId = threadId,
                    question = "안녕하세요",
                    answer = "안녕하세요! 무엇을 도와드릴까요?",
                    createdAt = now
                ),
                Chat(
                    id = 2L,
                    threadId = threadId,
                    question = "날씨 어때?",
                    answer = "오늘 날씨는 맑습니다.",
                    createdAt = now
                )
            )

            val thread = ChatThread(
                id = threadId,
                userId = userId,
                lastActivityAt = now,
                createdAt = now
            )

            val user = User(
                id = userId,
                email = "test@example.com",
                password = "encoded",
                name = "테스트유저",
                createdAt = now
            )

            whenever(chatRepository.findByCreatedAtBetween(any(), any())).thenReturn(chats)
            whenever(chatThreadRepository.findById(threadId)).thenReturn(thread)
            whenever(userRepository.findById(userId)).thenReturn(user)

            // when
            val result = reportService.generateChatReport()

            // then
            val csvContent = String(result, StandardCharsets.UTF_8)

            assertTrue(csvContent.contains("chat_id,user_id,user_email,user_name,question,answer,created_at"))
            assertTrue(csvContent.contains("test@example.com"))
            assertTrue(csvContent.contains("테스트유저"))
            assertTrue(csvContent.contains("안녕하세요"))
            assertTrue(csvContent.contains("날씨 어때?"))
        }

        @Test
        @DisplayName("성공: 대화가 없을 때 헤더만 있는 CSV 반환")
        fun generateChatReport_noChats_returnsHeaderOnly() {
            // given
            whenever(chatRepository.findByCreatedAtBetween(any(), any())).thenReturn(emptyList())

            // when
            val result = reportService.generateChatReport()

            // then
            val csvContent = String(result, StandardCharsets.UTF_8)

            assertTrue(csvContent.contains("chat_id,user_id,user_email,user_name,question,answer,created_at"))
            val lines = csvContent.trim().lines()
            assertEquals(1, lines.size)
        }

        @Test
        @DisplayName("성공: CSV 특수문자 이스케이프 처리")
        fun generateChatReport_escapesSpecialCharacters() {
            // given
            val now = OffsetDateTime.now()
            val userId = 1L
            val threadId = 100L

            val chats = listOf(
                Chat(
                    id = 1L,
                    threadId = threadId,
                    question = "쉼표, 포함된 질문",
                    answer = "따옴표\"가 포함된 답변",
                    createdAt = now
                )
            )

            val thread = ChatThread(
                id = threadId,
                userId = userId,
                lastActivityAt = now,
                createdAt = now
            )

            val user = User(
                id = userId,
                email = "test@example.com",
                password = "encoded",
                name = "홍길동",
                createdAt = now
            )

            whenever(chatRepository.findByCreatedAtBetween(any(), any())).thenReturn(chats)
            whenever(chatThreadRepository.findById(threadId)).thenReturn(thread)
            whenever(userRepository.findById(userId)).thenReturn(user)

            // when
            val result = reportService.generateChatReport()

            // then
            val csvContent = String(result, StandardCharsets.UTF_8)

            assertTrue(csvContent.contains("\"쉼표, 포함된 질문\""))
            assertTrue(csvContent.contains("\"따옴표\"\"가 포함된 답변\""))
        }

        @Test
        @DisplayName("성공: 스레드나 사용자를 찾을 수 없을 때 빈 값으로 처리")
        fun generateChatReport_missingThreadOrUser_handlesGracefully() {
            // given
            val now = OffsetDateTime.now()

            val chats = listOf(
                Chat(
                    id = 1L,
                    threadId = 999L,
                    question = "질문",
                    answer = "답변",
                    createdAt = now
                )
            )

            whenever(chatRepository.findByCreatedAtBetween(any(), any())).thenReturn(chats)
            whenever(chatThreadRepository.findById(999L)).thenReturn(null)

            // when
            val result = reportService.generateChatReport()

            // then
            val csvContent = String(result, StandardCharsets.UTF_8)

            assertTrue(csvContent.contains("chat_id,user_id,user_email,user_name,question,answer,created_at"))
            assertTrue(csvContent.contains("질문"))
            assertTrue(csvContent.contains("답변"))
        }

        @Test
        @DisplayName("성공: 여러 스레드의 대화 처리")
        fun generateChatReport_multipleThreads_success() {
            // given
            val now = OffsetDateTime.now()

            val chats = listOf(
                Chat(id = 1L, threadId = 100L, question = "질문1", answer = "답변1", createdAt = now),
                Chat(id = 2L, threadId = 200L, question = "질문2", answer = "답변2", createdAt = now)
            )

            val thread1 = ChatThread(id = 100L, userId = 1L, lastActivityAt = now, createdAt = now)
            val thread2 = ChatThread(id = 200L, userId = 2L, lastActivityAt = now, createdAt = now)

            val user1 = User(id = 1L, email = "user1@example.com", password = "encoded", name = "유저1", createdAt = now)
            val user2 = User(id = 2L, email = "user2@example.com", password = "encoded", name = "유저2", createdAt = now)

            whenever(chatRepository.findByCreatedAtBetween(any(), any())).thenReturn(chats)
            whenever(chatThreadRepository.findById(100L)).thenReturn(thread1)
            whenever(chatThreadRepository.findById(200L)).thenReturn(thread2)
            whenever(userRepository.findById(1L)).thenReturn(user1)
            whenever(userRepository.findById(2L)).thenReturn(user2)

            // when
            val result = reportService.generateChatReport()

            // then
            val csvContent = String(result, StandardCharsets.UTF_8)

            assertTrue(csvContent.contains("user1@example.com"))
            assertTrue(csvContent.contains("user2@example.com"))
            assertTrue(csvContent.contains("유저1"))
            assertTrue(csvContent.contains("유저2"))

            val lines = csvContent.trim().lines()
            assertEquals(3, lines.size)
        }
    }
}
