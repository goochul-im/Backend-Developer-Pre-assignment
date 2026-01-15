package com.assignment.report.adapter.`in`.web

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val adminEmail = "admin@example.com"
    private val memberEmail = "member@example.com"
    private val testPassword = "password123"

    private lateinit var adminToken: String
    private lateinit var memberToken: String

    @BeforeEach
    fun setUp() {
        userJpaRepository.deleteAll()

        val encodedPassword = passwordEncoder.encode(testPassword)

        val adminUser = UserJpaEntity(
            email = adminEmail,
            password = encodedPassword,
            name = "관리자",
            role = Role.ADMIN
        )
        userJpaRepository.save(adminUser)

        val memberUser = UserJpaEntity(
            email = memberEmail,
            password = encodedPassword,
            name = "일반회원",
            role = Role.MEMBER
        )
        userJpaRepository.save(memberUser)

        adminToken = login(adminEmail)
        memberToken = login(memberEmail)
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
    @DisplayName("GET /api/reports/activity-stats - 사용자 활동 기록 조회")
    inner class GetActivityStatsTest {

        @Test
        @DisplayName("성공: 관리자가 활동 기록 조회")
        fun getActivityStats_asAdmin_returns200() {
            mockMvc.perform(
                get("/api/reports/activity-stats")
                    .header("Authorization", "Bearer $adminToken")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.signUpCount").isNumber)
                .andExpect(jsonPath("$.data.loginCount").isNumber)
                .andExpect(jsonPath("$.data.chatCount").isNumber)
        }

        @Test
        @DisplayName("실패: 일반 회원이 활동 기록 조회 시도")
        fun getActivityStats_asMember_returns403() {
            mockMvc.perform(
                get("/api/reports/activity-stats")
                    .header("Authorization", "Bearer $memberToken")
            )
                .andDo(print())
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("실패: 인증 없이 활동 기록 조회 시도")
        fun getActivityStats_withoutAuth_returns401() {
            mockMvc.perform(
                get("/api/reports/activity-stats")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("실패: 잘못된 토큰으로 활동 기록 조회 시도")
        fun getActivityStats_withInvalidToken_returns401() {
            mockMvc.perform(
                get("/api/reports/activity-stats")
                    .header("Authorization", "Bearer invalid.token.here")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("GET /api/reports/chats/csv - CSV 보고서 다운로드")
    inner class GenerateChatReportTest {

        @Test
        @DisplayName("성공: 관리자가 CSV 보고서 다운로드")
        fun generateChatReport_asAdmin_returns200() {
            mockMvc.perform(
                get("/api/reports/chats/csv")
                    .header("Authorization", "Bearer $adminToken")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().exists("Content-Disposition"))
        }

        @Test
        @DisplayName("실패: 일반 회원이 CSV 보고서 다운로드 시도")
        fun generateChatReport_asMember_returns403() {
            mockMvc.perform(
                get("/api/reports/chats/csv")
                    .header("Authorization", "Bearer $memberToken")
            )
                .andDo(print())
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("실패: 인증 없이 CSV 보고서 다운로드 시도")
        fun generateChatReport_withoutAuth_returns401() {
            mockMvc.perform(
                get("/api/reports/chats/csv")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("실패: 잘못된 토큰으로 CSV 보고서 다운로드 시도")
        fun generateChatReport_withInvalidToken_returns401() {
            mockMvc.perform(
                get("/api/reports/chats/csv")
                    .header("Authorization", "Bearer invalid.token.here")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }
    }
}
