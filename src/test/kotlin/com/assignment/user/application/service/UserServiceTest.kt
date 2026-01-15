package com.assignment.user.application.service

import com.assignment.common.exception.BusinessException
import com.assignment.common.exception.ErrorCode
import com.assignment.user.application.port.`in`.LoginUseCase
import com.assignment.user.application.port.`in`.SignUpUseCase
import com.assignment.user.application.port.out.PasswordEncoder
import com.assignment.user.application.port.out.TokenProvider
import com.assignment.user.application.port.out.UserRepository
import com.assignment.user.domain.Role
import com.assignment.user.domain.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.OffsetDateTime

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var tokenProvider: TokenProvider

    @InjectMocks
    private lateinit var userService: UserService

    private val testEmail = "test@example.com"
    private val testPassword = "password123"
    private val testName = "테스트유저"
    private val encodedPassword = "encodedPassword123"

    @Nested
    @DisplayName("회원가입 테스트")
    inner class SignUpTest {

        @Test
        @DisplayName("성공: 유효한 정보로 회원가입")
        fun signUp_withValidInfo_success() {
            // given
            val command = SignUpUseCase.SignUpCommand(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            val savedUser = User(
                id = 1L,
                email = testEmail,
                password = encodedPassword,
                name = testName,
                createdAt = OffsetDateTime.now(),
                role = Role.MEMBER
            )

            whenever(userRepository.existsByEmail(testEmail)).thenReturn(false)
            whenever(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword)
            whenever(userRepository.save(any())).thenReturn(savedUser)

            // when
            val result = userService.signUp(command)

            // then
            assertNotNull(result)
            assertEquals(testEmail, result.email)
            assertEquals(testName, result.name)
            assertEquals(Role.MEMBER, result.role)

            verify(userRepository).existsByEmail(testEmail)
            verify(passwordEncoder).encode(testPassword)
            verify(userRepository).save(any())
        }

        @Test
        @DisplayName("실패: 이미 존재하는 이메일로 회원가입")
        fun signUp_withDuplicateEmail_throwsException() {
            // given
            val command = SignUpUseCase.SignUpCommand(
                email = testEmail,
                password = testPassword,
                name = testName
            )

            whenever(userRepository.existsByEmail(testEmail)).thenReturn(true)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                userService.signUp(command)
            }

            assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.errorCode)
            verify(userRepository).existsByEmail(testEmail)
            verify(userRepository, never()).save(any())
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    inner class LoginTest {

        @Test
        @DisplayName("성공: 유효한 자격증명으로 로그인")
        fun login_withValidCredentials_success() {
            // given
            val command = LoginUseCase.LoginCommand(
                email = testEmail,
                password = testPassword
            )

            val existingUser = User(
                id = 1L,
                email = testEmail,
                password = encodedPassword,
                name = testName,
                createdAt = OffsetDateTime.now(),
                role = Role.MEMBER
            )

            val expectedToken = "jwt.token.here"
            val expectedExpiresIn = 3600L

            whenever(userRepository.findByEmail(testEmail)).thenReturn(existingUser)
            whenever(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true)
            whenever(tokenProvider.createToken(testEmail, Role.MEMBER.name)).thenReturn(expectedToken)
            whenever(tokenProvider.getExpirationSeconds()).thenReturn(expectedExpiresIn)

            // when
            val result = userService.login(command)

            // then
            assertNotNull(result)
            assertEquals(expectedToken, result.accessToken)
            assertEquals(expectedExpiresIn, result.expiresIn)

            verify(userRepository).findByEmail(testEmail)
            verify(passwordEncoder).matches(testPassword, encodedPassword)
            verify(tokenProvider).createToken(testEmail, Role.MEMBER.name)
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 로그인")
        fun login_withNonExistentEmail_throwsException() {
            // given
            val command = LoginUseCase.LoginCommand(
                email = testEmail,
                password = testPassword
            )

            whenever(userRepository.findByEmail(testEmail)).thenReturn(null)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                userService.login(command)
            }

            assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.errorCode)
            verify(userRepository).findByEmail(testEmail)
            verify(passwordEncoder, never()).matches(any(), any())
        }

        @Test
        @DisplayName("실패: 잘못된 비밀번호로 로그인")
        fun login_withWrongPassword_throwsException() {
            // given
            val command = LoginUseCase.LoginCommand(
                email = testEmail,
                password = "wrongPassword"
            )

            val existingUser = User(
                id = 1L,
                email = testEmail,
                password = encodedPassword,
                name = testName,
                createdAt = OffsetDateTime.now(),
                role = Role.MEMBER
            )

            whenever(userRepository.findByEmail(testEmail)).thenReturn(existingUser)
            whenever(passwordEncoder.matches("wrongPassword", encodedPassword)).thenReturn(false)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                userService.login(command)
            }

            assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.errorCode)
            verify(userRepository).findByEmail(testEmail)
            verify(passwordEncoder).matches("wrongPassword", encodedPassword)
            verify(tokenProvider, never()).createToken(any(), any())
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    inner class GetUserTest {

        @Test
        @DisplayName("성공: 이메일로 사용자 조회")
        fun getUserByEmail_withExistingEmail_success() {
            // given
            val existingUser = User(
                id = 1L,
                email = testEmail,
                password = encodedPassword,
                name = testName,
                createdAt = OffsetDateTime.now(),
                role = Role.MEMBER
            )

            whenever(userRepository.findByEmail(testEmail)).thenReturn(existingUser)

            // when
            val result = userService.getUserByEmail(testEmail)

            // then
            assertNotNull(result)
            assertEquals(testEmail, result.email)
            assertEquals(testName, result.name)
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일로 사용자 조회")
        fun getUserByEmail_withNonExistentEmail_throwsException() {
            // given
            whenever(userRepository.findByEmail(testEmail)).thenReturn(null)

            // when & then
            val exception = assertThrows(BusinessException::class.java) {
                userService.getUserByEmail(testEmail)
            }

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }
    }
}
