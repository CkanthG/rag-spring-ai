package com.ai.rag.config

import com.ai.rag.tools.AgentTools
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfig {

    @Bean
    @Qualifier("agentChatClient")
    fun agentChatClient(chatModel: ChatModel, agentTools: AgentTools): ChatClient =
        ChatClient.builder(chatModel)
            .defaultSystem("""...""".trimIndent())
            .defaultTools(agentTools)
            .build()
}