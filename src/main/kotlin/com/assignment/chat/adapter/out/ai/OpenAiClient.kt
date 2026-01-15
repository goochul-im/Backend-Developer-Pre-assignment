package com.assignment.chat.adapter.out.ai

import com.assignment.chat.application.port.out.AiClient
import com.assignment.chat.domain.Chat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class OpenAiClient(
    @Value("\${openai.api-key}") private val apiKey: String,
    @Value("\${openai.model}") private val defaultModel: String,
    @Value("\${openai.base-url}") private val baseUrl: String
) : AiClient {

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .codecs { it.defaultCodecs().maxInMemorySize(1024 * 1024) }
        .build()

    private val objectMapper = jacksonObjectMapper()

    override fun generateAnswer(
        question: String,
        conversationHistory: List<Chat>,
        model: String?,
        isStreaming: Boolean
    ): AiClient.AiResponse {
        if (apiKey.isBlank()) {
            return AiClient.AiResponse(
                answer = "OpenAI API 키가 설정되지 않았습니다. OPENAI_API_KEY 환경 변수를 설정해주세요.",
                isStreaming = false
            )
        }

        val messages = buildMessages(question, conversationHistory)
        val requestBody = ChatCompletionRequest(
            model = model ?: defaultModel,
            messages = messages,
            stream = false
        )

        return try {
            val response = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(ChatCompletionResponse::class.java)
                .block()

            val answer = response?.choices?.firstOrNull()?.message?.content
                ?: "AI 응답을 받지 못했습니다."

            AiClient.AiResponse(
                answer = answer,
                isStreaming = false
            )
        } catch (e: Exception) {
            AiClient.AiResponse(
                answer = "AI 응답 생성 중 오류가 발생했습니다: ${e.message}",
                isStreaming = false
            )
        }
    }

    private fun buildMessages(question: String, conversationHistory: List<Chat>): List<Message> {
        val messages = mutableListOf<Message>()

        messages.add(
            Message(
                role = "system",
                content = "당신은 친절하고 도움이 되는 AI 어시스턴트입니다. 사용자의 질문에 정확하고 유용한 답변을 제공해주세요."
            )
        )

        conversationHistory.forEach { chat ->
            messages.add(Message(role = "user", content = chat.question))
            messages.add(Message(role = "assistant", content = chat.answer))
        }

        messages.add(Message(role = "user", content = question))

        return messages
    }

    // Request/Response DTOs
    data class ChatCompletionRequest(
        val model: String,
        val messages: List<Message>,
        val stream: Boolean = false
    )

    data class Message(
        val role: String,
        val content: String
    )

    data class ChatCompletionResponse(
        val id: String?,
        val `object`: String?,
        val created: Long?,
        val model: String?,
        val choices: List<Choice>?,
        val usage: Usage?
    )

    data class Choice(
        val index: Int?,
        val message: MessageResponse?,
        val finish_reason: String?
    )

    data class MessageResponse(
        val role: String?,
        val content: String?
    )

    data class Usage(
        val prompt_tokens: Int?,
        val completion_tokens: Int?,
        val total_tokens: Int?
    )

}
