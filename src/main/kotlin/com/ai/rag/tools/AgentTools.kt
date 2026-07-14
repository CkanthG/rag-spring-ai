package com.ai.rag.tools

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class AgentTools(
    private val vectorStore: VectorStore
) {

    @Tool(description = "Get today's date")
    fun getCurrentDate(): String = LocalDate.now().toString()

    @Tool(description = "Search internal knowledge base documents for relevant information")
    fun searchKnowledgeBase(
        @ToolParam(description = "The query to search for") query: String
    ): String {
        val results = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.0)
                .build()
        )

        return results.joinToString { "Document ID: ${it.id}, Score: ${it.score}, Content: ${it.text ?: ""}" }.ifEmpty { "No relevant documents found." }
    }

    @Tool(description = "Calculate number of business days between two dates")
    fun calculateBusinessDays(
        @ToolParam(description = "Start date in yyyy-MM-dd format") startDate: String,
        @ToolParam(description = "End date in yyyy-MM-dd format") endDate: String
    ): Int {
        val start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
        val end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
        return generateSequence(start) { it.plusDays(1) }.takeWhile { it.isBefore(end) || it.isEqual(end) }.count { it.dayOfWeek.value < 6 }
    }
}