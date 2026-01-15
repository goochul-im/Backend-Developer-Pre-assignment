package com.assignment.report.application.service

import com.assignment.chat.application.port.out.ChatRepository
import com.assignment.report.application.port.`in`.GenerateReportUseCase
import com.assignment.report.application.port.`in`.GetActivityStatsUseCase
import com.assignment.thread.application.port.out.ChatThreadRepository
import com.assignment.user.application.port.out.LoginHistoryRepository
import com.assignment.user.application.port.out.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class ReportService(
    private val userRepository: UserRepository,
    private val loginHistoryRepository: LoginHistoryRepository,
    private val chatRepository: ChatRepository,
    private val chatThreadRepository: ChatThreadRepository
) : GetActivityStatsUseCase, GenerateReportUseCase {

    override fun getActivityStats(): GetActivityStatsUseCase.ActivityStats {
        val now = OffsetDateTime.now()
        val oneDayAgo = now.minusDays(1)

        val signUpCount = userRepository.countByCreatedAtBetween(oneDayAgo, now)
        val loginCount = loginHistoryRepository.countByLoginAtBetween(oneDayAgo, now)
        val chatCount = chatRepository.countByCreatedAtBetween(oneDayAgo, now)

        return GetActivityStatsUseCase.ActivityStats(
            signUpCount = signUpCount,
            loginCount = loginCount,
            chatCount = chatCount
        )
    }

    override fun generateChatReport(): ByteArray {
        val now = OffsetDateTime.now()
        val oneDayAgo = now.minusDays(1)

        val chats = chatRepository.findByCreatedAtBetween(oneDayAgo, now)

        val threadIds = chats.map { it.threadId }.distinct()
        val threadUserMap = threadIds.associateWith { threadId ->
            chatThreadRepository.findById(threadId)?.userId
        }

        val userIds = threadUserMap.values.filterNotNull().distinct()
        val userMap = userIds.associateWith { userId ->
            userRepository.findById(userId)
        }

        val outputStream = ByteArrayOutputStream()
        OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
            writer.write('\ufeff'.code)

            writer.write("chat_id,user_id,user_email,user_name,question,answer,created_at\n")

            val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

            chats.forEach { chat ->
                val userId = threadUserMap[chat.threadId]
                val user = userId?.let { userMap[it] }

                val row = listOf(
                    chat.id.toString(),
                    userId?.toString() ?: "",
                    escapeCsv(user?.email ?: ""),
                    escapeCsv(user?.name ?: ""),
                    escapeCsv(chat.question),
                    escapeCsv(chat.answer),
                    chat.createdAt.format(dateFormatter)
                ).joinToString(",")

                writer.write(row)
                writer.write("\n")
            }
        }

        return outputStream.toByteArray()
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
