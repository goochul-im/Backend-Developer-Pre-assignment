package com.assignment.chat.application.service

import com.assignment.chat.application.port.`in`.CreateChatUseCase
import com.assignment.chat.application.port.out.AiClient
import com.assignment.chat.application.port.out.ChatRepository
import com.assignment.chat.domain.Chat
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
class ChatServiceTest {

    @Mock
    private lateinit var chatRepository: ChatRepository

    @Mock
    private lateinit var chatThreadRepository: ChatThreadRepository

    @Mock
    private lateinit var aiClient: AiClient

    @InjectMocks
    private lateinit var chatService: ChatService

    private val testUserId = 1L
    private val testThreadId = 100L
    private val testQuestion = "안녕하세요?"
    private val testAnswer = "안녕하세요! 무엇을 도와드릴까요?"

    @Nested
    @DisplayName("대화 생성 테스트")
    inner class CreateChatTest {

        @Test
        @DisplayName("성공: 첫 질문 시 새 스레드 생성 후 대화 저장")
        fun createChat_firstQuestion_createsNewThread() {
            // given
            val command = CreateChatUseCase.CreateChatCommand(
                userId = testUserId,
                question = testQuestion
            )

            val newThread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val savedChat = Chat(
                id = 1L,
                threadId = testThreadId,
                question = testQuestion,
                answer = testAnswer,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatThreadRepository.findLatestByUserId(testUserId)).thenReturn(null)
            whenever(chatThreadRepository.save(any<ChatThread>())).thenReturn(newThread)
            whenever(chatRepository.findByThreadId(testThreadId)).thenReturn(emptyList())
            whenever(aiClient.generateAnswer(eq(testQuestion), eq(emptyList()), isNull(), eq(false)))
                .thenReturn(AiClient.AiResponse(answer = testAnswer))
            whenever(chatRepository.save(any<Chat>())).thenReturn(savedChat)

            // when
            val result = chatService.createChat(command)

            // then
            assertNotNull(result)
            assertEquals(testQuestion, result.question)
            assertEquals(testAnswer, result.answer)

            verify(chatThreadRepository).findLatestByUserId(testUserId)
            verify(chatThreadRepository).save(any<ChatThread>())
            verify(chatRepository).save(any<Chat>())
        }

        @Test
        @DisplayName("성공: 30분 이내 질문 시 기존 스레드 사용")
        fun createChat_within30Minutes_usesExistingThread() {
            // given
            val command = CreateChatUseCase.CreateChatCommand(
                userId = testUserId,
                question = testQuestion
            )

            val existingThread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now().minusMinutes(10), // 10분 전
                createdAt = OffsetDateTime.now().minusHours(1)
            )

            val savedChat = Chat(
                id = 1L,
                threadId = testThreadId,
                question = testQuestion,
                answer = testAnswer,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatThreadRepository.findLatestByUserId(testUserId)).thenReturn(existingThread)
            whenever(chatRepository.findByThreadId(testThreadId)).thenReturn(emptyList())
            whenever(aiClient.generateAnswer(eq(testQuestion), eq(emptyList()), isNull(), eq(false)))
                .thenReturn(AiClient.AiResponse(answer = testAnswer))
            whenever(chatRepository.save(any<Chat>())).thenReturn(savedChat)

            // when
            val result = chatService.createChat(command)

            // then
            assertNotNull(result)
            assertEquals(testThreadId, result.threadId)

            verify(chatThreadRepository).findLatestByUserId(testUserId)
            verify(chatThreadRepository, never()).save(any<ChatThread>()) // 새 스레드 생성 안함
            verify(chatThreadRepository).updateLastActivityAt(eq(testThreadId), any())
        }

        @Test
        @DisplayName("성공: 30분 경과 후 질문 시 새 스레드 생성")
        fun createChat_after30Minutes_createsNewThread() {
            // given
            val command = CreateChatUseCase.CreateChatCommand(
                userId = testUserId,
                question = testQuestion
            )

            val expiredThread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now().minusMinutes(40), // 40분 전 (만료)
                createdAt = OffsetDateTime.now().minusHours(2)
            )

            val newThreadId = testThreadId + 1
            val newThread = ChatThread(
                id = newThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val savedChat = Chat(
                id = 1L,
                threadId = newThreadId,
                question = testQuestion,
                answer = testAnswer,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatThreadRepository.findLatestByUserId(testUserId)).thenReturn(expiredThread)
            whenever(chatThreadRepository.save(any<ChatThread>())).thenReturn(newThread)
            whenever(chatRepository.findByThreadId(newThreadId)).thenReturn(emptyList())
            whenever(aiClient.generateAnswer(eq(testQuestion), eq(emptyList()), isNull(), eq(false)))
                .thenReturn(AiClient.AiResponse(answer = testAnswer))
            whenever(chatRepository.save(any<Chat>())).thenReturn(savedChat)

            // when
            val result = chatService.createChat(command)

            // then
            assertNotNull(result)
            assertEquals(newThreadId, result.threadId)

            verify(chatThreadRepository).save(any<ChatThread>()) // 새 스레드 생성
            verify(chatThreadRepository, never()).updateLastActivityAt(any(), any())
        }

        @Test
        @DisplayName("성공: 모델 지정하여 대화 생성")
        fun createChat_withModel_passesModelToAiClient() {
            // given
            val customModel = "gpt-4"
            val command = CreateChatUseCase.CreateChatCommand(
                userId = testUserId,
                question = testQuestion,
                model = customModel
            )

            val newThread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )

            val savedChat = Chat(
                id = 1L,
                threadId = testThreadId,
                question = testQuestion,
                answer = testAnswer,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatThreadRepository.findLatestByUserId(testUserId)).thenReturn(null)
            whenever(chatThreadRepository.save(any<ChatThread>())).thenReturn(newThread)
            whenever(chatRepository.findByThreadId(testThreadId)).thenReturn(emptyList())
            whenever(aiClient.generateAnswer(eq(testQuestion), eq(emptyList()), eq(customModel), eq(false)))
                .thenReturn(AiClient.AiResponse(answer = testAnswer))
            whenever(chatRepository.save(any<Chat>())).thenReturn(savedChat)

            // when
            chatService.createChat(command)

            // then
            verify(aiClient).generateAnswer(eq(testQuestion), eq(emptyList()), eq(customModel), eq(false))
        }

        @Test
        @DisplayName("성공: 대화 이력이 AI에 전달됨")
        fun createChat_withHistory_passesHistoryToAiClient() {
            // given
            val command = CreateChatUseCase.CreateChatCommand(
                userId = testUserId,
                question = testQuestion
            )

            val existingThread = ChatThread(
                id = testThreadId,
                userId = testUserId,
                lastActivityAt = OffsetDateTime.now().minusMinutes(5),
                createdAt = OffsetDateTime.now().minusHours(1)
            )

            val previousChats = listOf(
                Chat(id = 1L, threadId = testThreadId, question = "이전 질문", answer = "이전 답변", createdAt = OffsetDateTime.now().minusMinutes(10)),
                Chat(id = 2L, threadId = testThreadId, question = "또 다른 질문", answer = "또 다른 답변", createdAt = OffsetDateTime.now().minusMinutes(5))
            )

            val savedChat = Chat(
                id = 3L,
                threadId = testThreadId,
                question = testQuestion,
                answer = testAnswer,
                createdAt = OffsetDateTime.now()
            )

            whenever(chatThreadRepository.findLatestByUserId(testUserId)).thenReturn(existingThread)
            whenever(chatRepository.findByThreadId(testThreadId)).thenReturn(previousChats)
            whenever(aiClient.generateAnswer(eq(testQuestion), eq(previousChats), isNull(), eq(false)))
                .thenReturn(AiClient.AiResponse(answer = testAnswer))
            whenever(chatRepository.save(any<Chat>())).thenReturn(savedChat)

            // when
            chatService.createChat(command)

            // then
            verify(aiClient).generateAnswer(eq(testQuestion), eq(previousChats), isNull(), eq(false))
        }
    }

    @Nested
    @DisplayName("대화 목록 조회 테스트")
    inner class GetChatsTest {

        @Test
        @DisplayName("성공: 사용자별 대화 목록 조회")
        fun getChatsByUser_returnsThreadsWithChats() {
            // given
            val pageable = PageRequest.of(0, 20)

            val threads = listOf(
                ChatThread(id = 1L, userId = testUserId, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now()),
                ChatThread(id = 2L, userId = testUserId, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now())
            )
            val threadsPage = PageImpl(threads, pageable, threads.size.toLong())

            val chats = listOf(
                Chat(id = 1L, threadId = 1L, question = "질문1", answer = "답변1", createdAt = OffsetDateTime.now()),
                Chat(id = 2L, threadId = 1L, question = "질문2", answer = "답변2", createdAt = OffsetDateTime.now()),
                Chat(id = 3L, threadId = 2L, question = "질문3", answer = "답변3", createdAt = OffsetDateTime.now())
            )

            whenever(chatThreadRepository.findByUserId(testUserId, pageable)).thenReturn(threadsPage)
            whenever(chatRepository.findByThreadIds(listOf(1L, 2L))).thenReturn(chats)

            // when
            val result = chatService.getChatsByUser(testUserId, pageable)

            // then
            assertEquals(2, result.content.size)
            assertEquals(2, result.content[0].chats.size) // 첫 번째 스레드에 2개 채팅
            assertEquals(1, result.content[1].chats.size) // 두 번째 스레드에 1개 채팅
        }

        @Test
        @DisplayName("성공: 전체 대화 목록 조회")
        fun getAllChats_returnsAllThreadsWithChats() {
            // given
            val pageable = PageRequest.of(0, 20)

            val threads = listOf(
                ChatThread(id = 1L, userId = 1L, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now()),
                ChatThread(id = 2L, userId = 2L, lastActivityAt = OffsetDateTime.now(), createdAt = OffsetDateTime.now())
            )
            val threadsPage = PageImpl(threads, pageable, threads.size.toLong())

            val chats = listOf(
                Chat(id = 1L, threadId = 1L, question = "질문1", answer = "답변1", createdAt = OffsetDateTime.now()),
                Chat(id = 2L, threadId = 2L, question = "질문2", answer = "답변2", createdAt = OffsetDateTime.now())
            )

            whenever(chatThreadRepository.findAll(pageable)).thenReturn(threadsPage)
            whenever(chatRepository.findByThreadIds(listOf(1L, 2L))).thenReturn(chats)

            // when
            val result = chatService.getAllChats(pageable)

            // then
            assertEquals(2, result.content.size)
            assertEquals(2, result.totalElements)
        }

        @Test
        @DisplayName("성공: 스레드가 없을 때 빈 결과 반환")
        fun getChatsByUser_noThreads_returnsEmptyPage() {
            // given
            val pageable = PageRequest.of(0, 20)
            val emptyPage = PageImpl<ChatThread>(emptyList(), pageable, 0)

            whenever(chatThreadRepository.findByUserId(testUserId, pageable)).thenReturn(emptyPage)

            // when
            val result = chatService.getChatsByUser(testUserId, pageable)

            // then
            assertTrue(result.content.isEmpty())
            assertEquals(0, result.totalElements)
            verify(chatRepository, never()).findByThreadIds(any())
        }
    }
}
