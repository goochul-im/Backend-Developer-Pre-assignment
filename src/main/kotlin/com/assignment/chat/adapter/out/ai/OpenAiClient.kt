package com.assignment.chat.adapter.out.ai

import com.assignment.chat.application.port.out.AiClient
import com.assignment.chat.domain.Chat
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Component

@Component
class OpenAiClient(
    private val chatModel: ChatModel
) : AiClient {

    override fun generateAnswer(
        question: String,
        conversationHistory: List<Chat>,
        model: String?,
        isStreaming: Boolean
    ): AiClient.AiResponse {

        val messages = buildMessages(question, conversationHistory)

        val optionBuilder = OpenAiChatOptions.builder()
        if (!model.isNullOrBlank()) {
            optionBuilder.model(model)
        }
        val option = optionBuilder.build()

        val prompt = Prompt(messages, option)

        val response = chatModel.call(prompt)

        val answer = response.result.output.text ?: "응답을 생성할 수 없습니다."

        return AiClient.AiResponse(
            answer = answer
        )
    }

    private fun buildMessages(question: String, conversationHistory: List<Chat>): List<Message> {
        val messages = mutableListOf<Message>()

        messages.add(
            SystemMessage("당신은 친절하고 도움이 되는 AI 어시스턴트입니다. 사용자의 질문에 정확하고 유용한 답변을 제공해주세요.")
        )

        conversationHistory.forEach { chat ->
            messages.add(UserMessage(chat.question))
            messages.add(AssistantMessage(chat.answer))
        }

        messages.add(UserMessage(question))

        return messages
    }
}
