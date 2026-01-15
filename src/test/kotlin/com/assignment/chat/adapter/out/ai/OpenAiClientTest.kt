package com.assignment.chat.adapter.out.ai

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class OpenAiClientTest {

    @Autowired
    lateinit var client: OpenAiClient

    @Test
    fun `Openai로부터 응답을 받아올 수 있다`(){
        //given
        val question = "1+1은 얼마인가요? 숫자로만 대답하고 이외의 다른 대답은 절대 하지 마세요"


        //when
        val result = client.generateAnswer(question, emptyList(), null, isStreaming = false)
        println("AI의 대답 : ${result.answer}")

        //then
        assertThat(result.answer).isEqualTo("2")
    }

}
