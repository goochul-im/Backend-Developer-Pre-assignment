package com.assignment.thread.application.service

import com.assignment.common.exception.BusinessException
import com.assignment.common.exception.ErrorCode
import com.assignment.thread.application.port.`in`.DeleteChatThreadUseCase
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
class ChatThreadServiceTest {

    @Mock
    private lateinit var chatThreadRepository: ChatThreadRepository

    @InjectMocks
    private lateinit var chatThreadService: ChatThreadService

    private val testUserId = 1L
    private val testThreadId = 100L

    @Nested
    @DisplayName("스레드 조회 테스트")
    inner class GetChatThreadsTest {

        @Test
        @DisplayName("성공: 사용자별 스레드 목록 조회")
        fun getChatThreadsByUser_returnsUserThreads() {
            // given
            val pageable = PageRequest.of(0, 20)
            val threads = listOf(
                ChatThread(id = 1L, userId = testUserId, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now()),
                ChatThread(id = 2L, userId = testUserId, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now())
            )
            val threadsPage = PageImpl(threads, pageable, threads.size.toLong())

            whenever(chatThreadRepository.findByUserId(testUserId, pageable)).thenReturn(threadsPage)

            // when
            val result = chatThreadService.getChatThreadsByUser(testUserId, pageable)

            // then
            assertEquals(2, result.content.size)
            assertTrue(result.content.all { it.userId == testUserId })
            verify(chatThreadRepository).findByUserId(testUserId, pageable)
        }

        @Test
        @DisplayName("성공: 전체 스레드 목록 조회")
        fun getAllChatThreads_returnsAllThreads() {
            // given
            val pageable = PageRequest.of(0, 20)
            val threads = listOf(
                ChatThread(id = 1L, userId = 1L, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now()),
                ChatThread(id = 2L, userId = 2L, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now()),
                ChatThread(id = 3L, userId = 3L, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now())
            )
            val threadsPage = PageImpl(threads, pageable, threads.size.toLong())

            whenever(chatThreadRepository.findAll(pageable)).thenReturn(threadsPage)

            // when
            val result = chatThreadService.getAllChatThreads(pageable)

            // then
            assertEquals(3, result.content.size)
            verify(chatThreadRepository).findAll(pageable)
        }

        @Test
        @DisplayName("성공: ID로 스레드 조회")
        fun getChatThreadById_existingId_returnsThread() {
            // given
            val thread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(thread)

            // when
            val result = chatThreadService.getChatThreadById(testThreadId)

            // then
            assertNotNull(result)
            assertEquals(testThreadId, result.id)
            assertEquals(testUserId, result.userId)
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 스레드 조회")
        fun getChatThreadById_nonExistingId_throwsException() {
            // given
            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(null)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                chatThreadService.getChatThreadById(testThreadId)
            }

            assertEquals(ErrorCode.THREAD_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("스레드 삭제 테스트")
    inner class DeleteChatThreadTest {

        @Test
        @DisplayName("성공: 본인 스레드 삭제")
        fun deleteChatThread_ownThread_deletesSuccessfully() {
            // given
            val thread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val command = DeleteChatThreadUseCase.DeleteChatThreadCommand(
                threadId = testThreadId,
                requesterId = testUserId
            )

            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(thread)

            // when
            chatThreadService.deleteChatThread(command)

            // then
            verify(chatThreadRepository).deleteById(testThreadId)
        }

        @Test
        @DisplayName("실패: 존재하지 않는 스레드 삭제 시도")
        fun deleteChatThread_nonExistingThread_throwsException() {
            // given
            val command = DeleteChatThreadUseCase.DeleteChatThreadCommand(
                threadId = testThreadId,
                requesterId = testUserId
            )

            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(null)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                chatThreadService.deleteChatThread(command)
            }

            assertEquals(ErrorCode.THREAD_NOT_FOUND, exception.errorCode)
            verify(chatThreadRepository, never()).deleteById(any())
        }

        @Test
        @DisplayName("실패: 다른 사용자의 스레드 삭제 시도")
        fun deleteChatThread_otherUserThread_throwsException() {
            // given
            val otherUserId = 999L
            val thread = ChatThread(
                id = testThreadId,
                userId = otherUserId, // 다른 사용자의 스레드
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val command = DeleteChatThreadUseCase.DeleteChatThreadCommand(
                threadId = testThreadId,
                requesterId = testUserId // 현재 사용자
            )

            whenever(chatThreadRepository.findById(testThreadId)).thenReturn(thread)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                chatThreadService.deleteChatThread(command)
            }

            assertEquals(ErrorCode.THREAD_ACCESS_DENIED, exception.errorCode)
            verify(chatThreadRepository, never()).deleteById(any())
        }
    }
}
