package com.assignment.report.application.port.`in`

interface GetActivityStatsUseCase {
    fun getActivityStats(): ActivityStats

    data class ActivityStats(
        val signUpCount: Long,
        val loginCount: Long,
        val chatCount: Long
    )
}
