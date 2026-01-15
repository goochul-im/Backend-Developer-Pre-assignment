package com.assignment.user.adapter.`in`.web

import com.assignment.user.adapter.`in`.web.dto.LoginRequest
import com.assignment.user.adapter.`in`.web.dto.SignUpRequest
import com.assignment.user.adapter.out.persistence.UserJpaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    private val testEmail = "test@example.com"
    private val testPassword = "password123"
    private val testName = "테스트유저"

    @BeforeEach
    fun setUp() {
        userJpaRepository.deleteAll()
    }

    @Nested
    @DisplayName("POST /api/auth/signup - 회원가입")
    inner class SignUpTest {

        @Test
        @DisplayName("성공: 유효한 정보로 회원가입")
        fun signUp_withValidInfo_returns201() {
            // given
            val request = SignUpRequest(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            // when & then
            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(testEmail))
                .andExpect(jsonPath("$.data.name").value(testName))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.createdAt").exists())
        }

        @Test
        @DisplayName("실패: 이메일 중복")
        fun signUp_withDuplicateEmail_returns409() {
            // given - 먼저 사용자 등록
            val request = SignUpRequest(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            ).andExpect(status().isCreated)

            // when & then - 같은 이메일로 다시 등록
            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_001"))
        }

        @Test
        @DisplayName("실패: 이메일 형식 오류")
        fun signUp_withInvalidEmail_returns400() {
            // given
            val request = SignUpRequest(
                email = "invalid-email",
                password = testPassword,
                name = testName
            )

            // when & then
            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_001"))
        }

        @Test
        @DisplayName("실패: 비밀번호 길이 부족")
        fun signUp_withShortPassword_returns400() {
            // given
            val request = SignUpRequest(
                email = testEmail,
                password = "short",
                name = testName
            )

            // when & then
            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        @DisplayName("실패: 이름 누락")
        fun signUp_withEmptyName_returns400() {
            // given
            val request = SignUpRequest(
                email = testEmail,
                password = testPassword,
                name = ""
            )

            // when & then
            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 로그인")
    inner class LoginTest {

        @BeforeEach
        fun setUpUser() {
            // 테스트용 사용자 등록
            val signUpRequest = SignUpRequest(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest))
            ).andExpect(status().isCreated)
        }

        @Test
        @DisplayName("성공: 유효한 자격증명으로 로그인")
        fun login_withValidCredentials_returns200() {
            // given
            val request = LoginRequest(
                email = testEmail,
                password = testPassword
            )

            // when & then
            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").exists())
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 로그인")
        fun login_withNonExistentEmail_returns401() {
            // given
            val request = LoginRequest(
                email = "nonexistent@example.com",
                password = testPassword
            )

            // when & then
            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_003"))
        }

        @Test
        @DisplayName("실패: 잘못된 비밀번호로 로그인")
        fun login_withWrongPassword_returns401() {
            // given
            val request = LoginRequest(
                email = testEmail,
                password = "wrongPassword"
            )

            // when & then
            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_003"))
        }
    }

    @Nested
    @DisplayName("GET /api/users/me - 내 정보 조회")
    inner class GetCurrentUserTest {

        @Test
        @DisplayName("성공: 유효한 토큰으로 내 정보 조회")
        fun getCurrentUser_withValidToken_returns200() {
            // given - 회원가입 및 로그인
            val signUpRequest = SignUpRequest(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            mockMvc.perform(
                post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest))
            ).andExpect(status().isCreated)

            val loginRequest = LoginRequest(
                email = testEmail,
                password = testPassword
            )

            val loginResult = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            )
                .andExpect(status().isOk)
                .andReturn()

            val responseBody = objectMapper.readTree(loginResult.response.contentAsString)
            val accessToken = responseBody.get("data").get("accessToken").asText()

            // when & then
            mockMvc.perform(
                get("/api/users/me")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andDo(print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(testEmail))
                .andExpect(jsonPath("$.data.name").value(testName))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
        }

        @Test
        @DisplayName("실패: 토큰 없이 내 정보 조회")
        fun getCurrentUser_withoutToken_returns401() {
            // when & then
            mockMvc.perform(
                get("/api/users/me")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("실패: 잘못된 토큰으로 내 정보 조회")
        fun getCurrentUser_withInvalidToken_returns401() {
            // when & then
            mockMvc.perform(
                get("/api/users/me")
                    .header("Authorization", "Bearer invalid.token.here")
            )
                .andDo(print())
                .andExpect(status().isUnauthorized)
        }
    }
}
