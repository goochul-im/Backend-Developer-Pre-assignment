package com.assignment.feedback.application.service

import com.assignment.chat.application.port.out.ChatRepository
import com.assignment.chat.domain.Chat
import com.assignment.common.exception.BusinessException
import com.assignment.common.exception.ErrorCode
import com.assignment.feedback.application.port.`in`.CreateFeedbackUseCase
import com.assignment.feedback.application.port.`in`.UpdateFeedbackStatusUseCase
import com.assignment.feedback.application.port.out.FeedbackRepository
import com.assignment.feedback.domain.Feedback
import com.assignment.feedback.domain.FeedbackStatus
import com.assignment.thread.application.port.out.ChatThreadRepository
import com.assignment.thread.domain.ChatThread
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {

    @Mock
    private lateinit var feedbackRepository: FeedbackRepository

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var chatThreadRepository: ChatThreadRepository

    @InjectMocks
    private lateinit var feedbackService: FeedbackService

    private val testUserId = 1L
    private val testChatId = 10L
    private val testThreadId = 100L
    private val testFeedbackId = 1000L

    @Nested
    @DisplayName("피드백 생성 테스트")
    inner class CreateFeedbackTest {

        @Test
        @DisplayName("성공: 본인 대화에 피드백 생성")
        fun createFeedback_ownChat_success() {
            // given
            val command = CreateFeedbackUseCase.CreateFeedbackCommand(
                userId = testUserId,
                chatId = testChatId,
                isPositive = true,
                isAdmin = false
            )

            val chat = Chat(
                id = testChatId,
                threadId = testThreadId,
                question = "질문",
                answer = "답변",
                createdAt = OffsetDateTime.now()
            )

            val thread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val savedFeedback = Feedback(
                id = testFeedbackId,
                userId = testUserId,
                chatId = testChatId,
                isPositive = true,
                status = FeedbackStatus.PENDING,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatRepository.findById(testChatId)).thenReturn(chat)
            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(thread)
            whenever(feedbackRepository.findByUserIdAndChatId(testUserId, testChatId)).thenReturn(null)
            whenever(feedbackRepository.save(any<Feedback>())).thenReturn(savedFeedback)

            // when
            val result = feedbackService.createFeedback(command)

            // then
            assertNotNull(result)
            assertEquals(testFeedbackId, result.id)
            assertEquals(testUserId, result.userId)
            assertEquals(testChatId, result.chatId)
            assertTrue(result.isPositive)
            assertEquals(FeedbackStatus.PENDING, result.status)

            verify(feedbackRepository).save(any<Feedback>())
        }

        @Test
        @DisplayName("성공: 관리자가 다른 사용자 대화에 피드백 생성")
        fun createFeedback_adminOnOthersChat_success() {
            // given
            val adminUserId = 999L
            val command = CreateFeedbackUseCase.CreateFeedbackCommand(
                userId = adminUserId,
                chatId = testChatId,
                isPositive = false,
                isAdmin = true
            )

            val chat = Chat(
                id = testChatId,
                threadId = testThreadId,
                question = "질문",
                answer = "답변",
                createdAt = OffsetDateTime.now()
            )

            val thread = ChatThread(
                id = testThreadId,
                userId = testUserId, // 다른 사용자의 스레드
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val savedFeedback = Feedback(
                id = testFeedbackId,
                userId = adminUserId,
                chatId = testChatId,
                isPositive = false,
                status = FeedbackStatus.PENDING,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatRepository.findById(testChatId)).thenReturn(chat)
            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(thread)
            whenever(feedbackRepository.findByUserIdAndChatId(adminUserId, testChatId)).thenReturn(null)
            whenever(feedbackRepository.save(any<Feedback>())).thenReturn(savedFeedback)

            // when
            val result = feedbackService.createFeedback(command)

            // then
            assertNotNull(result)
            assertEquals(adminUserId, result.userId)
            assertFalse(result.isPositive)
        }

        @Test
        @DisplayName("실패: 대화를 찾을 수 없음")
        fun createFeedback_chatNotFound_throwsException() {
            // given
            val command = CreateFeedbackUseCase.CreateFeedbackCommand(
                userId = testUserId,
                chatId = testChatId,
                isPositive = true
            )

            whenever(chatRepository.findById(testChatId)).thenReturn(null)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                feedbackService.createFeedback(command)
            }

            assertEquals(ErrorCode.CHAT_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("실패: 스레드를 찾을 수 없음")
        fun createFeedback_threadNotFound_throwsException() {
            // given
            val command = CreateFeedbackUseCase.CreateFeedbackCommand(
                userId = testUserId,
                chatId = testChatId,
                isPositive = true
            )

            val chat = Chat(
                id = testChatId,
                threadId = testThreadId,
                question = "질문",
                answer = "답변",
                createdAt = OffsetDateTime.now()
            )

            whenever(chatRepository.findById(testChatId)).thenReturn(chat)
            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(null)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                feedbackService.createFeedback(command)
            }

            assertEquals(ErrorCode.THREAD_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("실패: 본인 대화가 아닌 경우 접근 거부")
        fun createFeedback_notOwnChat_throwsException() {
            // given
            val otherUserId = 999L
            val command = CreateFeedbackUseCase.CreateFeedbackCommand(
                userId = testUserId,
                chatId = testChatId,
                isPositive = true,
                isAdmin = false
            )

            val chat = Chat(
                id = testChatId,
                threadId = testThreadId,
                question = "질문",
                answer = "답변",
                createdAt = OffsetDateTime.now()
            )

            val thread = ChatThread(
                id = testThreadId,
                userId = otherUserId, // 다른 사용자의 스레드
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            whenever(chatRepository.findById(testChatId)).thenReturn(chat)
            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(thread)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                feedbackService.createFeedback(command)
            }

            assertEquals(ErrorCode.FEEDBACK_ACCESS_DENIED, exception.errorCode)
        }

        @Test
        @DisplayName("실패: 이미 피드백이 존재하는 경우")
        fun createFeedback_alreadyExists_throwsException() {
            // given
            val command = CreateFeedbackUseCase.CreateFeedbackCommand(
                userId = testUserId,
                chatId = testChatId,
                isPositive = true
            )

            val chat = Chat(
                id = testChatId,
                threadId = testThreadId,
                question = "질문",
                answer = "답변",
                createdAt = OffsetDateTime.now()
            )

            val thread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val existingFeedback = Feedback(
                id = testFeedbackId,
                userId = testUserId,
                chatId = testChatId,
                isPositive = false,
                status = FeedbackStatus.PENDING,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatRepository.findById(testChatId)).thenReturn(chat)
            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(thread)
            whenever(feedbackRepository.findByUserIdAndChatId(testUserId, testChatId)).thenReturn(existingFeedback)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                feedbackService.createFeedback(command)
            }

            assertEquals(ErrorCode.FEEDBACK_ALREADY_EXISTS, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("피드백 목록 조회 테스트")
    inner class QueryFeedbacksTest {

        @Test
        @DisplayName("성공: 사용자별 피드백 목록 조회")
        fun getFeedbacksByUser_success() {
            // given
            val pageable = PageRequest.of(0, 20)
            val feedbacks = listOf(
                Feedback(id = 1L, userId = testUserId, chatId = 1L, isPositive = true, createdAt = OffsetDateTime.now()),
                Feedback(id = 2L, userId = testUserId, chatId = 2L, isPositive = false, createdAt = OffsetDateTime.now())
            )
            val feedbacksPage = PageImpl(feedbacks, pageable, feedbacks.size.toLong())

            whenever(feedbackRepository.findByUserId(testUserId, null, pageable)).thenReturn(feedbacksPage)

            // when
            val result = feedbackService.getFeedbacksByUser(testUserId, null, pageable)

            // then
            assertEquals(2, result.content.size)
            assertEquals(2, result.totalElements)
        }

        @Test
        @DisplayName("성공: 긍정 피드백만 필터링")
        fun getFeedbacksByUser_filterPositive_success() {
            // given
            val pageable = PageRequest.of(0, 20)
            val positiveFeedbacks = listOf(
                Feedback(id = 1L, userId = testUserId, chatId = 1L, isPositive = true, createdAt = OffsetDateTime.now())
            )
            val feedbacksPage = PageImpl(positiveFeedbacks, pageable, positiveFeedbacks.size.toLong())

            whenever(feedbackRepository.findByUserId(testUserId, true, pageable)).thenReturn(feedbacksPage)

            // when
            val result = feedbackService.getFeedbacksByUser(testUserId, true, pageable)

            // then
            assertEquals(1, result.content.size)
            assertTrue(result.content[0].isPositive)
        }

        @Test
        @DisplayName("성공: 전체 피드백 목록 조회")
        fun getAllFeedbacks_success() {
            // given
            val pageable = PageRequest.of(0, 20)
            val feedbacks = listOf(
                Feedback(id = 1L, userId = 1L, chatId = 1L, isPositive = true, createdAt = OffsetDateTime.now()),
                Feedback(id = 2L, userId = 2L, chatId = 2L, isPositive = false, createdAt = OffsetDateTime.now()),
                Feedback(id = 3L, userId = 3L, chatId = 3L, isPositive = true, createdAt = OffsetDateTime.now())
            )
            val feedbacksPage = PageImpl(feedbacks, pageable, feedbacks.size.toLong())

            whenever(feedbackRepository.findAll(null, pageable)).thenReturn(feedbacksPage)

            // when
            val result = feedbackService.getAllFeedbacks(null, pageable)

            // then
            assertEquals(3, result.content.size)
            assertEquals(3, result.totalElements)
        }

        @Test
        @DisplayName("성공: 부정 피드백만 필터링하여 전체 조회")
        fun getAllFeedbacks_filterNegative_success() {
            // given
            val pageable = PageRequest.of(0, 20)
            val negativeFeedbacks = listOf(
                Feedback(id = 2L, userId = 2L, chatId = 2L, isPositive = false, createdAt = OffsetDateTime.now())
            )
            val feedbacksPage = PageImpl(negativeFeedbacks, pageable, negativeFeedbacks.size.toLong())

            whenever(feedbackRepository.findAll(false, pageable)).thenReturn(feedbacksPage)

            // when
            val result = feedbackService.getAllFeedbacks(false, pageable)

            // then
            assertEquals(1, result.content.size)
            assertFalse(result.content[0].isPositive)
        }
    }

    @Nested
    @DisplayName("피드백 상태 변경 테스트")
    inner class UpdateFeedbackStatusTest {

        @Test
        @DisplayName("성공: 상태를 RESOLVED로 변경")
        fun updateStatus_toResolved_success() {
            // given
            val command = UpdateFeedbackStatusUseCase.UpdateFeedbackStatusCommand(
                feedbackId = testFeedbackId,
                status = FeedbackStatus.RESOLVED
            )

            val existingFeedback = Feedback(
                id = testFeedbackId,
                userId = testUserId,
                chatId = testChatId,
                isPositive = true,
                status = FeedbackStatus.PENDING,
                createdAt = OffsetDateTime.now()
            )

            val updatedFeedback = existingFeedback.updateStatus(FeedbackStatus.RESOLVED)

            whenever(feedbackRepository.findById(testFeedbackId)).thenReturn(existingFeedback)
            whenever(feedbackRepository.save(any<Feedback>())).thenReturn(updatedFeedback)

            // when
            val result = feedbackService.updateStatus(command)

            // then
            assertEquals(FeedbackStatus.RESOLVED, result.status)
            verify(feedbackRepository).save(any<Feedback>())
        }

        @Test
        @DisplayName("성공: 상태를 PENDING으로 변경")
        fun updateStatus_toPending_success() {
            // given
            val command = UpdateFeedbackStatusUseCase.UpdateFeedbackStatusCommand(
                feedbackId = testFeedbackId,
                status = FeedbackStatus.PENDING
            )

            val existingFeedback = Feedback(
                id = testFeedbackId,
                userId = testUserId,
                chatId = testChatId,
                isPositive = false,
                status = FeedbackStatus.RESOLVED,
                createdAt = OffsetDateTime.now()
            )

            val updatedFeedback = existingFeedback.updateStatus(FeedbackStatus.PENDING)

            whenever(feedbackRepository.findById(testFeedbackId)).thenReturn(existingFeedback)
            whenever(feedbackRepository.save(any<Feedback>())).thenReturn(updatedFeedback)

            // when
            val result = feedbackService.updateStatus(command)

            // then
            assertEquals(FeedbackStatus.PENDING, result.status)
        }

        @Test
        @DisplayName("실패: 피드백을 찾을 수 없음")
        fun updateStatus_feedbackNotFound_throwsException() {
            // given
            val command = UpdateFeedbackStatusUseCase.UpdateFeedbackStatusCommand(
                feedbackId = testFeedbackId,
                status = FeedbackStatus.RESOLVED
            )

            whenever(feedbackRepository.findById(testFeedbackId)).thenReturn(null)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                feedbackService.updateStatus(command)
            }

            assertEquals(ErrorCode.FEEDBACK_NOT_FOUND, exception.errorCode)
        }
    }
}
