package com.assignment.user.adapter.out.persistence

import com.assignment.user.application.port.out.LoginHistoryRepository
import com.assignment.user.domain.LoginHistory
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class LoginHistoryRepositoryAdapter(
    private val loginHistoryJpaRepository: LoginHistoryJpaRepository
) : LoginHistoryRepository {

    override fun save(loginHistory: LoginHistory): LoginHistory {
        val entity = LoginHistoryJpaEntity.from(loginHistory)
        val savedEntity = loginHistoryJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun countByLoginAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long {
        return loginHistoryJpaRepository.countByLoginAtBetween(start, end)
    }
}
