package com.assignment.report.adapter.`in`.web

import com.assignment.common.response.ApiResponse
import com.assignment.report.adapter.`in`.web.dto.ActivityStatsResponse
import com.assignment.report.application.port.`in`.GenerateReportUseCase
import com.assignment.report.application.port.`in`.GetActivityStatsUseCase
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('ADMIN')")
class ReportController(
    private val getActivityStatsUseCase: GetActivityStatsUseCase,
    private val generateReportUseCase: GenerateReportUseCase
) {

    @GetMapping("/activity-stats")
    fun getActivityStats(): ResponseEntity<ApiResponse<ActivityStatsResponse>> {
        val stats = getActivityStatsUseCase.getActivityStats()
        return ResponseEntity.ok(ApiResponse.success(ActivityStatsResponse.from(stats)))
    }

    @GetMapping("/chats/csv")
    fun generateChatReport(): ResponseEntity<ByteArray> {
        val csvBytes = generateReportUseCase.generateChatReport()
        val filename = "chat_report_${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}.csv"

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvBytes)
    }

}
