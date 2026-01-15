package com.assignment.report.adapter.`in`.web.dto

import com.assignment.report.application.port.`in`.GetActivityStatsUseCase

data class ActivityStatsResponse(
    val signUpCount: Long,
    val loginCount: Long,
    val chatCount: Long
) {
    companion object {
        fun from(stats: GetActivityStatsUseCase.ActivityStats): ActivityStatsResponse {
            return ActivityStatsResponse(
                signUpCount = stats.signUpCount,
                loginCount = stats.loginCount,
                chatCount = stats.chatCount
            )
        }
    }
}
