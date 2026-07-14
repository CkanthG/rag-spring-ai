package com.ai.rag.ingestion

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayOutputStream
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class DocumentIngestionServiceTest {

    @Test
    fun `ingest accepts pdf files and stores chunks`() {
        val vectorStore = Mockito.mock(VectorStore::class.java)
        val service = DocumentIngestionService(vectorStore)
        val pdfFile = MockMultipartFile(
            "file",
            "sample.pdf",
            "application/pdf",
            createPdfBytes("PDF content for RAG ingestion test")
        )

        val chunkCount = service.ingest(pdfFile)

        assertTrue(chunkCount > 0)
        Mockito.verify(vectorStore).add(anyList())
    }

    @Test
    fun `ingest rejects unsupported file type`() {
        val vectorStore = Mockito.mock(VectorStore::class.java)
        val service = DocumentIngestionService(vectorStore)
        val binaryFile = MockMultipartFile("file", "image.png", "image/png", byteArrayOf(1, 2, 3))

        assertFailsWith<IllegalArgumentException> {
            service.ingest(binaryFile)
        }
    }

    private fun createPdfBytes(text: String): ByteArray {
        val output = ByteArrayOutputStream()
        PDDocument().use { document ->
            val page = PDPage()
            document.addPage(page)

            PDPageContentStream(document, page).use { contentStream ->
                contentStream.beginText()
                contentStream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
                contentStream.newLineAtOffset(50f, 700f)
                contentStream.showText(text)
                contentStream.endText()
            }

            document.save(output)
        }
        return output.toByteArray()
    }
}

