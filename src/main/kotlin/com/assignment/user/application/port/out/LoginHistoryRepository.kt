package com.assignment.user.application.port.out

import com.assignment.user.domain.LoginHistory
import java.time.OffsetDateTime

interface LoginHistoryRepository {
    fun save(loginHistory: LoginHistory): LoginHistory
    fun countByLoginAtBetween(start: OffsetDateTime, end: OffsetDateTime): Long
}
