# RAG Spring AI

A Kotlin + Spring Boot RAG (Retrieval-Augmented Generation) application using:
- Ollama for LLM + embeddings
- PostgreSQL + PGVector for vector storage
- Spring AI `QuestionAnswerAdvisor` for retrieval grounding

## What this app does

1. Ingests PDF and text files and stores vector embeddings.
2. Retrieves relevant chunks from PGVector.
3. Answers questions using retrieved context.

## Tech stack

- Java 21
- Kotlin 2.2.x
- Spring Boot 4.0.x
- Spring AI 2.0.x
- Apache PDFBox 3.0.3
- PostgreSQL (`pgvector/pgvector:pg17`)
- Ollama (`ollama/ollama:latest`)

## Prerequisites

- Docker + Docker Compose
- Java 21
- (Optional) `curl` for API testing

## Configuration notes

Current defaults in `src/main/resources/application.yaml`:
- `spring.docker.compose.enabled: false`
  - The app **does not auto-start Docker Compose**. Start containers manually.
- Chat model: `llama3.2:1b`
- Embedding model: `nomic-embed-text`
- DB URL: `jdbc:postgresql://localhost:5432/ragdb`

## 1) Start dependencies

From project root:

```bash
cd /home/devansh/rag-spring-ai
docker-compose up -d
```

Check services:

```bash
docker-compose ps
```

Expected mapped ports from `compose.yaml`:
- Ollama: `11434`
- PostgreSQL: `5432`

## 2) Pull Ollama models (first run)

```bash
docker-compose exec -T ollama ollama pull llama3.2:1b
docker-compose exec -T ollama ollama pull nomic-embed-text
```

Optional health check:

```bash
curl -s http://localhost:11434/api/tags
```

## 3) Run the app

```bash
cd /home/devansh/rag-spring-ai
./gradlew bootRun
```

The app runs on:
- `http://localhost:8080`

## 4) Ingest a document

Endpoint:
- `POST /api/v1/documents/ingest`

Supported file types:
- PDF: `application/pdf`, `.pdf`
- Text: `text/*`, `.txt`, `.md`, `.markdown`, `.csv`, `.log`

Example:

```bash
curl -X POST "http://localhost:8080/api/v1/documents/ingest" \
  -F "file=@onboarding-guide.md"
```

PDF example:

```bash
curl -X POST "http://localhost:8080/api/v1/documents/ingest" \
  -F "file=@sample.pdf"
```

Example response:

```json
{
  "filename": "sample.pdf",
  "chunksIndexed": 12
}
```

Validation error response example:

```json
{
  "error": "Unsupported file type. Please upload a PDF or text file."
}
```

## 5) Ask a RAG question

Endpoint:
- `POST /api/v1/chat/ask?question=...`

Example:

```bash
curl -X POST "http://localhost:8080/api/v1/chat/ask?question=What%20is%20the%20onboarding%20process%3F"
```

Example response:

```json
{
  "answer": "..."
}
```

## Run tests

```bash
cd /home/devansh/rag-spring-ai
./gradlew test
```

## Troubleshooting

### Ollama model load fails with `signal: killed`

Cause: insufficient memory for selected model.

Fixes:
- Use smaller chat model (already set to `llama3.2:1b`).
- Ensure Docker has enough memory.
- Pull model explicitly:

```bash
docker-compose exec -T ollama ollama pull llama3.2:1b
```

### App fails at startup due to DB connection

Ensure PG container is healthy:

```bash
docker-compose ps
```

You should see `pgvector` in `Up (healthy)` state.

### Ingestion request returns `400 Bad Request`

The API returns clear validation messages for common upload issues:
- `Uploaded file is empty.`
- `Unsupported file type. Please upload a PDF or text file.`
- `No readable content found in uploaded file.`

### Spring Docker Compose auto-management issues

This project keeps `spring.docker.compose.enabled: false` to avoid local CLI compatibility issues. Start/stop dependencies manually with Docker Compose.

## Stop services

```bash
cd /home/devansh/rag-spring-ai
docker-compose down
```

