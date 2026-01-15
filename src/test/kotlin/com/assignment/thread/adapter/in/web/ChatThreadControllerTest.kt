package com.assignment.thread.adapter.`in`.web

import com.assignment.chat.adapter.out.persistence.ChatJpaEntity
import com.assignment.chat.adapter.out.persistence.ChatJpaRepository
import com.assignment.thread.adapter.out.persistence.ChatThreadJpaEntity
import com.assignment.thread.adapter.out.persistence.ChatThreadJpaRepository
import com.assignment.user.adapter.`in`.web.dto.LoginRequest
import com.assignment.user.adapter.out.persistence.UserJpaEntity
import com.assignment.user.adapter.out.persistence.UserJpaRepository
import com.assignment.user.domain.Role
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatThreadControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Autowired
    private lateinit var chatJpaRepository: ChatJpaRepository

    @Autowired
    private lateinit var chatThreadJpaRepository: ChatThreadJpaRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val adminEmail = "admin@example.com"
    private val member1Email = "member1@example.com"
    private val member2Email = "member2@example.com"
    private val testPassword = "password123"

    private lateinit var adminToken: String
    private lateinit var member1Token: String
    private lateinit var member2Token: String

    private var adminId: Long = 0
    private var member1Id: Long = 0
    private var member2Id: Long = 0

    @BeforeEach
    fun setUp() {
        chatJpaRepository.deleteAll()
        chatThreadJpaRepository.deleteAll()
        userJpaRepository.deleteAll()

        val encodedPassword = passwordEncoder.encode(testPassword)

        val adminUser = userJpaRepository.save(
            UserJpaEntity(
                email = adminEmail,
                password = encodedPassword,
                name = "관리자",
                role = Role.ADMIN
            )
        )
        adminId = adminUser.id!!

        val member1User = userJpaRepository.save(
            UserJpaEntity(
                email = member1Email,
                password = encodedPassword,
                name = "회원1",
                role = Role.MEMBER
            )
        )
        member1Id = member1User.id!!

        val member2User = userJpaRepository.save(
            UserJpaEntity(
                email = member2Email,
                password = encodedPassword,
                name = "회원2",
                role = Role.MEMBER
            )
        )
        member2Id = member2User.id!!

        adminToken = login(adminEmail)
        member1Token = login(member1Email)
        member2Token = login(member2Email)
    }

    private fun login(email: String): String {
        val loginRequest = LoginRequest(email = email, password = testPassword)

        val result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val responseBody = objectMapper.readTree(result.response.contentAsString)
        return responseBody.get("data").get("accessToken").asText()
    }

    private fun createThread(userId: Long): ChatThreadJpaEntity {
        return chatThreadJpaRepository.save(
            ChatThreadJpaEntity(
                userId = userId,
                lastActivityAt = OffsetDateTime.now(),
                createdAt = OffsetDateTime.now()
            )
        )
    }

    private fun createChat(threadId: Long): ChatJpaEntity {
        return chatJpaRepository.save(
            ChatJpaEntity(
                threadId = threadId,
                question = "테스트 질문",
                answer = "테스트 답변",
                createdAt = OffsetDateTime.now()
            )
        )
    }

    @Nested
    @DisplayName("DELETE /api/threads/{threadId} - 스레드 삭제")
    inner class DeleteChatThreadTest {

        @Test
        @DisplayName("성공: 본인이 생성한 스레드 삭제")
        fun deleteChatThread_asOwner_returns200() {
            val thread = createThread(member1Id)
            createChat(thread.id!!)

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
                    .header("Authorization", "Bearer $member1Token")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))

            // 삭제 확인
            assert(chatThreadJpaRepository.findById(thread.id!!).isEmpty)
        }

        @Test
        @DisplayName("성공: 스레드 삭제 시 관련 채팅도 함께 삭제")
        fun deleteChatThread_deletesRelatedChats() {
            val thread = createThread(member1Id)
            val chat1 = createChat(thread.id!!)
            val chat2 = createChat(thread.id!!)

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
                    .header("Authorization", "Bearer $member1Token")
            )
                .andExpect(status().isOk)

            // 채팅도 삭제되었는지 확인
            assert(chatJpaRepository.findByThreadId(thread.id!!).isEmpty())
        }

        @Test
        @DisplayName("실패: 다른 회원의 스레드 삭제 시도")
        fun deleteChatThread_asOtherMember_returns403() {
            val thread = createThread(member1Id)

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
                    .header("Authorization", "Bearer $member2Token")
            )
                .andDo(print())
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("THREAD_002"))
        }

        @Test
        @DisplayName("실패: 관리자도 다른 회원의 스레드 삭제 불가")
        fun deleteChatThread_asAdmin_returns403() {
            val thread = createThread(member1Id)

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
                    .header("Authorization", "Bearer $adminToken")
            )
                .andDo(print())
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("THREAD_002"))
        }

        @Test
        @DisplayName("실패: 존재하지 않는 스레드 삭제 시도")
        fun deleteChatThread_notFound_returns404() {
            mockMvc.perform(
                delete("/api/threads/99999")
                    .header("Authorization", "Bearer $member1Token")
            )
                .andDo(print())
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("THREAD_001"))
        }

        @Test
        @DisplayName("실패: 인증 없이 스레드 삭제 시도")
        fun deleteChatThread_withoutAuth_returns401() {
            val thread = createThread(member1Id)

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("실패: 잘못된 토큰으로 스레드 삭제 시도")
        fun deleteChatThread_withInvalidToken_returns401() {
            val thread = createThread(member1Id)

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
                    .header("Authorization", "Bearer invalid.token.here")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("성공: 채팅이 없는 빈 스레드 삭제")
        fun deleteChatThread_emptyThread_returns200() {
            val thread = createThread(member1Id)
            // 채팅 없이 스레드만 생성

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
                    .header("Authorization", "Bearer $member1Token")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("성공: 관리자가 본인의 스레드 삭제")
        fun deleteChatThread_adminOwnsThread_returns200() {
            val thread = createThread(adminId)

            mockMvc.perform(
                delete("/api/threads/${thread.id}")
                    .header("Authorization", "Bearer $adminToken")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }
}
