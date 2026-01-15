package com.assignment.user.application.service

import com.assignment.common.exception.BusinessException
import com.assignment.common.exception.ErrorCode
import com.assignment.user.application.port.`in`.GetUserUseCase
import com.assignment.user.application.port.`in`.LoginUseCase
import com.assignment.user.application.port.`in`.SignUpUseCase
import com.assignment.user.application.port.out.PasswordEncoder
import com.assignment.user.application.port.out.TokenProvider
import com.assignment.user.application.port.out.UserRepository
import com.assignment.user.domain.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider
) : SignUpUseCase, LoginUseCase, GetUserUseCase {

    @Transactional
    override fun signUp(command: SignUpUseCase.SignUpCommand): User {
        if (userRepository.existsByEmail(command.email)) {
            throw BusinessException(ErrorCode.DUPLICATE_EMAIL)
        }

        val user = User(
            email = command.email,
            password = passwordEncoder.encode(command.password),
            name = command.name
        )

        return userRepository.save(user)
    }

    override fun login(command: LoginUseCase.LoginCommand): LoginUseCase.LoginResult {
        val user = userRepository.findByEmail(command.email)
            ?: throw BusinessException(ErrorCode.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(command.password, user.password)) {
            throw BusinessException(ErrorCode.INVALID_CREDENTIALS)
        }

        val token = tokenProvider.createToken(user.email, user.role.name)
        return LoginUseCase.LoginResult(
            accessToken = token,
            expiresIn = tokenProvider.getExpirationSeconds()
        )
    }

    override fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }
}
