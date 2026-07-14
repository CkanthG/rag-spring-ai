package com.ai.rag.controller

import com.ai.rag.ingestion.DocumentIngestionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/documents")
class IngestionController(
    private val documentIngestionService: DocumentIngestionService
) {

    @PostMapping("/ingest")
    fun ingestDocument(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, Any>> {
        return try {
            val chunkCount = documentIngestionService.ingest(file)
            val response = mapOf(
                "filename" to (file.originalFilename ?: "unknown"),
                "chunksIndexed" to chunkCount
            )
            ResponseEntity.ok(response)
        }
        catch (ex: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to (ex.message ?: "Invalid file upload request.")))
        }
    }
}