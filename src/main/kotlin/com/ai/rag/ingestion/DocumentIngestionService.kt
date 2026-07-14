package com.ai.rag.ingestion

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.ai.document.Document
import org.springframework.ai.reader.TextReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.apply

@Service
class DocumentIngestionService(
    private val vectorStore: VectorStore,
    private val tokenTextSplitter: TokenTextSplitter = TokenTextSplitter.builder().build()
) {

    fun ingest(file: MultipartFile): Int {
        require(!file.isEmpty) { "Uploaded file is empty." }

        val source = file.originalFilename ?: "unknown"
        val rawDocuments = when {
            isPdf(file) -> listOf(Document(extractPdfText(file.bytes), mapOf("source" to source)))
            isTextFile(file) -> TextReader(ByteArrayResource(file.bytes)).apply {
                customMetadata["source"] = source
            }.get()
            else -> throw IllegalArgumentException("Unsupported file type. Please upload a PDF or text file.")
        }

        val chunks: List<Document> = tokenTextSplitter.split(rawDocuments)
        require(chunks.isNotEmpty()) { "No readable content found in uploaded file." }

        vectorStore.add(chunks)
        return chunks.size
    }

    private fun isPdf(file: MultipartFile): Boolean {
        val contentType = file.contentType?.lowercase()
        val fileName = file.originalFilename?.lowercase().orEmpty()
        return contentType == "application/pdf" || fileName.endsWith(".pdf")
    }

    private fun isTextFile(file: MultipartFile): Boolean {
        val contentType = file.contentType?.lowercase().orEmpty()
        val fileName = file.originalFilename?.lowercase().orEmpty()
        val textExtensions = setOf(".txt", ".md", ".markdown", ".csv", ".log")
        return contentType.startsWith("text/") || textExtensions.any { fileName.endsWith(it) }
    }

    private fun extractPdfText(pdfBytes: ByteArray): String {
        Loader.loadPDF(pdfBytes).use { pdfDocument ->
            val text = PDFTextStripper().getText(pdfDocument).trim()
            require(text.isNotBlank()) { "No readable text found in PDF file." }
            return text
        }
    }
}