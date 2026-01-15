package com.assignment.chat.adapter.`in`.web

import com.assignment.chat.adapter.`in`.web.dto.CreateChatRequest
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatControllerTest {

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

    @Nested
    @DisplayName("POST /api/chats - 대화 생성")
    inner class CreateChatTest {

        @Test
        @DisplayName("성공: 일반 회원이 대화 생성")
        fun createChat_asMember_returns201() {
            val request = CreateChatRequest(
                question = "안녕하세요, 테스트 질문입니다.",
                model = null,
                isStreaming = false
            )

            mockMvc.perform(
                post("/api/chats")
                    .header("Authorization", "Bearer $member1Token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.threadId").exists())
                .andExpect(jsonPath("$.data.question").value("안녕하세요, 테스트 질문입니다."))
                .andExpect(jsonPath("$.data.answer").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
        }

        @Test
        @DisplayName("성공: 관리자가 대화 생성")
        fun createChat_asAdmin_returns201() {
            val request = CreateChatRequest(
                question = "관리자의 테스트 질문입니다.",
                model = null,
                isStreaming = false
            )

            mockMvc.perform(
                post("/api/chats")
                    .header("Authorization", "Bearer $adminToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.question").value("관리자의 테스트 질문입니다."))
        }

        @Test
        @DisplayName("성공: 30분 이내 연속 질문 시 같은 스레드 유지")
        fun createChat_within30Minutes_sameThread() {
            val request1 = CreateChatRequest(question = "첫 번째 질문")

            val result1 = mockMvc.perform(
                post("/api/chats")
                    .header("Authorization", "Bearer $member1Token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request1))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val threadId1 = objectMapper.readTree(result1.response.contentAsString)
                .get("data").get("threadId").asLong()

            val request2 = CreateChatRequest(question = "두 번째 질문")

            val result2 = mockMvc.perform(
                post("/api/chats")
                    .header("Authorization", "Bearer $member1Token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request2))
            )
                .andExpect(status().isCreated)
                .andReturn()

            val threadId2 = objectMapper.readTree(result2.response.contentAsString)
                .get("data").get("threadId").asLong()

            assert(threadId1 == threadId2) { "30분 이내 연속 질문은 같은 스레드를 사용해야 합니다" }
        }

        @Test
        @DisplayName("실패: 인증 없이 대화 생성 시도")
        fun createChat_withoutAuth_returns401() {
            val request = CreateChatRequest(question = "테스트 질문")

            mockMvc.perform(
                post("/api/chats")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("실패: 잘못된 토큰으로 대화 생성 시도")
        fun createChat_withInvalidToken_returns401() {
            val request = CreateChatRequest(question = "테스트 질문")

            mockMvc.perform(
                post("/api/chats")
                    .header("Authorization", "Bearer invalid.token.here")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("실패: 빈 질문으로 대화 생성 시도")
        fun createChat_withEmptyQuestion_returns400() {
            val request = CreateChatRequest(question = "")

            mockMvc.perform(
                post("/api/chats")
                    .header("Authorization", "Bearer $member1Token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/chats - 대화 목록 조회")
    inner class GetChatsTest {

        @BeforeEach
        fun setUpChats() {
            // member1의 스레드와 채팅 생성
            val thread1 = chatThreadJpaRepository.save(
                ChatThreadJpaEntity(
                    userId = member1Id,
                    lastActivityAt = OffsetDateTime.now(),
                    createdAt = OffsetDateTime.now()
                )
            )
            chatJpaRepository.save(
                com.assignment.chat.adapter.out.persistence.ChatJpaEntity(
                    threadId = thread1.id!!,
                    question = "회원1의 질문1",
                    answer = "AI 답변1",
                    createdAt = OffsetDateTime.now()
                )
            )
            chatJpaRepository.save(
                com.assignment.chat.adapter.out.persistence.ChatJpaEntity(
                    threadId = thread1.id!!,
                    question = "회원1의 질문2",
                    answer = "AI 답변2",
                    createdAt = OffsetDateTime.now()
                )
            )

            // member2의 스레드와 채팅 생성
            val thread2 = chatThreadJpaRepository.save(
                ChatThreadJpaEntity(
                    userId = member2Id,
                    lastActivityAt = OffsetDateTime.now(),
                    createdAt = OffsetDateTime.now()
                )
            )
            chatJpaRepository.save(
                com.assignment.chat.adapter.out.persistence.ChatJpaEntity(
                    threadId = thread2.id!!,
                    question = "회원2의 질문",
                    answer = "AI 답변",
                    createdAt = OffsetDateTime.now()
                )
            )
        }

        @Test
        @DisplayName("성공: 일반 회원이 본인의 대화 목록 조회")
        fun getChats_asMember_returnsOwnChats() {
            mockMvc.perform(
                get("/api/chats")
                    .header("Authorization", "Bearer $member1Token")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray)
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].thread.userId").value(member1Id))
                .andExpect(jsonPath("$.data.content[0].chats.length()").value(2))
        }

        @Test
        @DisplayName("성공: 관리자가 전체 대화 목록 조회")
        fun getChats_asAdmin_returnsAllChats() {
            mockMvc.perform(
                get("/api/chats")
                    .header("Authorization", "Bearer $adminToken")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray)
                .andExpect(jsonPath("$.data.content.length()").value(2))
        }

        @Test
        @DisplayName("성공: 페이지네이션 적용")
        fun getChats_withPagination_returnsPagedResult() {
            mockMvc.perform(
                get("/api/chats")
                    .header("Authorization", "Bearer $adminToken")
                    .param("page", "0")
                    .param("size", "1")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
        }

        @Test
        @DisplayName("성공: 정렬 적용 (생성일시 오름차순)")
        fun getChats_withSortAsc_returnsSortedResult() {
            mockMvc.perform(
                get("/api/chats")
                    .header("Authorization", "Bearer $adminToken")
                    .param("sort", "createdAt,asc")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray)
        }

        @Test
        @DisplayName("성공: 대화가 없는 회원 조회 시 빈 목록 반환")
        fun getChats_noChats_returnsEmptyList() {
            // 새로운 회원의 토큰으로 조회 (대화 없음)
            chatJpaRepository.deleteAll()
            chatThreadJpaRepository.deleteAll()

            mockMvc.perform(
                get("/api/chats")
                    .header("Authorization", "Bearer $member1Token")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray)
                .andExpect(jsonPath("$.data.content.length()").value(0))
        }

        @Test
        @DisplayName("실패: 인증 없이 대화 목록 조회 시도")
        fun getChats_withoutAuth_returns401() {
            mockMvc.perform(
                get("/api/chats")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("실패: 잘못된 토큰으로 대화 목록 조회 시도")
        fun getChats_withInvalidToken_returns401() {
            mockMvc.perform(
                get("/api/chats")
                    .header("Authorization", "Bearer invalid.token.here")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }
    }
}
