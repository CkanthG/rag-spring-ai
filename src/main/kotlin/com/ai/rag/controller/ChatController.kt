package com.ai.rag.controller

import com.ai.rag.service.RagService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/chat")
class ChatController(
    private val ragService: RagService
) {

    @PostMapping("/ask")
    fun ask(@RequestParam question: String): Map<String, String> {
        return mapOf("answer" to ragService.ask(question))
    }
}