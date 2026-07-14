package com.ai.rag.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

@Service
class RagService(
    private val chatClient: ChatClient,
    private val vectorStore: VectorStore
) {

    fun ask(query: String): String {
        val qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(
                SearchRequest.builder()
                    .topK(5)
                    .similarityThreshold(0.5)
                    .build()
            )
            .build()
        return chatClient.prompt().user(query).advisors(qaAdvisor).call().content() ?: "No Response generated."
    }
}