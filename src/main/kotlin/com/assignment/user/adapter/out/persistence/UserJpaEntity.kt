package com.assignment.user.adapter.out.persistence

import com.assignment.user.domain.Role
import com.assignment.user.domain.User
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 255)
    val email: String,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: Role = Role.MEMBER
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        password = password,
        name = name,
        createdAt = createdAt,
        role = role
    )

    companion object {
        fun from(user: User): UserJpaEntity = UserJpaEntity(
            id = user.id,
            email = user.email,
            password = user.password,
            name = user.name,
            createdAt = user.createdAt,
            role = user.role
        )
    }
}
