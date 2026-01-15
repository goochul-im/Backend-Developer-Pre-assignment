package com.assignment.user.adapter.out.persistence

import com.assignment.user.domain.LoginHistory
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "login_histories")
class LoginHistoryJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "login_at", nullable = false)
    val loginAt: OffsetDateTime = OffsetDateTime.now()
) {
    fun toDomain(): LoginHistory = LoginHistory(
        id = id,
        userId = userId,
        loginAt = loginAt
    )

    companion object {
        fun from(loginHistory: LoginHistory): LoginHistoryJpaEntity = LoginHistoryJpaEntity(
            id = loginHistory.id,
            userId = loginHistory.userId,
            loginAt = loginHistory.loginAt
        )
    }
}
