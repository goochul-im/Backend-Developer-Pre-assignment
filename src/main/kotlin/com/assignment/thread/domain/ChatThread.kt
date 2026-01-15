package com.assignment.thread.domain

import java.time.OffsetDateTime

class ChatThread(
    val id: Long? = null,
    val userId: Long,
    var lastActivityAt: OffsetDateTime = OffsetDateTime.now(),
    val createdAt: OffsetDateTime = OffsetDateTime.now()
) {

    private val THREAD_TIMEOUT_MINUTES = 30L

    fun isExpired(time: OffsetDateTime) : Boolean {
        return this.lastActivityAt.plusMinutes(THREAD_TIMEOUT_MINUTES).isBefore(time)
    }

    fun updateLastActivityAt(time: OffsetDateTime) {
        this.lastActivityAt = time
    }

}
