package com.ai.rag.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {

    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient =
        ChatClient.builder(chatModel)
            .defaultSystem {
                """
                You are an internal knowledge-base assistant.
                Answer ONLY using the provided context documents.
                If the answer isn't in the context, say you don't have that information — do not guess.
                Always mention which source document(s) you drew from.
                """.trimIndent()
            }
            .defaultAdvisors { SimpleLoggerAdvisor() }
            .build()
}