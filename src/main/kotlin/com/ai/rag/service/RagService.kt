package com.ai.rag.service

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

@Service
class RagService(
    private val agentChatClient: ChatClient,
    private val vectorStore: VectorStore
) {

    // Strict RAG: always grounded in retrieved docs, refuses if not found
    fun ask(query: String): String {
        val qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
            .searchRequest(
                SearchRequest.builder()
                    .topK(3)
                    .similarityThreshold(0.0)
                    .build()
            )
            .build()
        return agentChatClient.prompt().user(query).advisors(qaAdvisor).call().content() ?: "No Response generated."
    }

    // Agentic: lets the model choose tools (including knowledge-base search) freely
    fun askAgent(query: String): String {
        return agentChatClient.prompt().user(query).call().content()
            ?: "No response generated."
    }
}