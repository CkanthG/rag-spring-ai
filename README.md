# RAG Spring AI

A Kotlin + Spring Boot Retrieval-Augmented Generation (RAG) service built with Spring AI.

It uses:
- **Ollama** for chat completion and embeddings
- **PostgreSQL + PGVector** for vector storage
- **Spring AI** for vector search, retrieval grounding, and tool-enabled chat

## What this app does

1. Accepts PDF and text-based files through an upload API.
2. Extracts and splits document content into chunks.
3. Stores embeddings in PGVector.
4. Answers questions in two modes:
   - **Strict RAG**: retrieves document context and grounds the answer in it.
   - **Agent mode**: allows the model to decide when to use tools, including knowledge-base search.

## Tech stack

- Java 21
- Kotlin 2.2.21
- Spring Boot 4.0.7
- Spring AI 2.0.0
- Apache PDFBox 3.0.3
- PostgreSQL with `pgvector/pgvector:pg17`
- Ollama with `ollama/ollama:latest`

## Project flow

### Ingestion flow

- `POST /api/v1/documents/ingest`
- Supports:
  - PDF: `application/pdf`, `.pdf`
  - Text: `text/*`, `.txt`, `.md`, `.markdown`, `.csv`, `.log`
- PDFs are parsed with PDFBox.
- Text files are read directly.
- Documents are split with Spring AI's `TokenTextSplitter`.
- Chunks are embedded and stored in PGVector with source metadata.

### Question answering flow

- `POST /api/v1/chat/ask`
  - Uses `QuestionAnswerAdvisor` with vector search.
  - Current retrieval settings: `topK=3`, `similarityThreshold=0.0`.
- `POST /api/v1/chat/agent`
  - Uses the same model with tool access enabled.
  - The model can call built-in tools such as knowledge-base search and business-day calculation.

## Built-in tools available to the agent

The agent-enabled endpoint can use these tools:

- `getCurrentDate()`
- `searchKnowledgeBase(query)`
- `calculateBusinessDays(startDate, endDate)`

## Prerequisites

- Java 21
- Docker Desktop or Docker Engine with Compose support
- Internet access for pulling container images and Ollama models on first run
- Optional: `curl` / `curl.exe` or Postman for API testing

## Configuration

Current defaults in `src/main/resources/application.yaml`:

| Property | Value |
| --- | --- |
| `spring.docker.compose.enabled` | `false` |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/ragdb` |
| `spring.datasource.username` | `postgres` |
| `spring.datasource.password` | `password` |
| `spring.ai.ollama.base-url` | `http://localhost:11434` |
| Chat model | `llama3.2:1b` |
| Embedding model | `nomic-embed-text` |
| PGVector dimensions | `768` |
| PGVector index type | `HNSW` |
| PGVector distance type | `COSINE_DISTANCE` |

### Important note about Docker Compose

`spring.docker.compose.enabled` is set to `false`, so the application **does not auto-start containers**. Start dependencies yourself before launching the app.

## Local development setup

### 1) Start dependencies

From the project root:

```powershell
docker compose up -d
```

Check service status:

```powershell
docker compose ps
```

Expected ports from `compose.yaml`:
- Ollama: `11434`
- PostgreSQL / PGVector: `5432`

### 2) Pull Ollama models on first run

```powershell
docker compose exec -T ollama ollama pull llama3.2:1b
docker compose exec -T ollama ollama pull nomic-embed-text
```

Optional Ollama health check:

```powershell
curl.exe http://localhost:11434/api/tags
```

### 3) Run the application

On Windows:

```powershell
.\gradlew.bat bootRun
```

On macOS / Linux:

```bash
./gradlew bootRun
```

The application starts on `http://localhost:8080`.

## API reference

### 1) Ingest a document

**Endpoint**

- `POST /api/v1/documents/ingest`

**Example files**

The repository already includes `onboarding-guide.md`, which is useful for a first ingestion test.

**Windows example**

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/documents/ingest" -F "file=@onboarding-guide.md"
```

**macOS / Linux example**

```bash
curl -X POST "http://localhost:8080/api/v1/documents/ingest" \
  -F "file=@onboarding-guide.md"
```

**Example response**

```json
{
  "filename": "onboarding-guide.md",
  "chunksIndexed": 12
}
```

**Possible validation errors**

```json
{
  "error": "Unsupported file type. Please upload a PDF or text file."
}
```

Other possible messages:
- `Uploaded file is empty.`
- `No readable content found in uploaded file.`
- `No readable text found in PDF file.`

### 2) Ask a strict RAG question

This endpoint always runs with retrieval grounding.

**Endpoint**

- `POST /api/v1/chat/ask?question=...`

**Example**

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/chat/ask?question=How%20much%20PTO%20do%20full-time%20employees%20receive%3F"
```

**Example response**

```json
{
  "answer": "..."
}
```

### 3) Ask an agent-enabled question

This endpoint can use tools, including knowledge-base search.

**Endpoint**

- `POST /api/v1/chat/agent?question=...`

**Examples**

Ask a knowledge-grounded question:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/chat/agent?question=What%20does%20the%20sample%20onboarding%20guide%20say%20about%20health%20benefits%3F"
```

Ask a tool-oriented question:

```powershell
curl.exe -X POST "http://localhost:8080/api/v1/chat/agent?question=How%20many%20business%20days%20are%20there%20between%202026-07-01%20and%202026-07-14%3F"
```

## Running tests

The current `@SpringBootTest` loads the full application context, so make sure the Docker dependencies are running first. In particular, PostgreSQL must be available and the `ragdb` database must exist.

On Windows:

```powershell
.\gradlew.bat test
```

On macOS / Linux:

```bash
./gradlew test
```

## Troubleshooting

### Ollama model pull or load fails

This is often caused by insufficient memory or a model that is too large for the machine.

Try:
- Keeping the configured chat model at `llama3.2:1b`
- Ensuring Docker has enough memory
- Pulling the models manually again

```powershell
docker compose exec -T ollama ollama pull llama3.2:1b
docker compose exec -T ollama ollama pull nomic-embed-text
```

### Application fails to connect to PostgreSQL

Verify that the `pgvector` container is healthy:

```powershell
docker compose ps
```

You should see the `pgvector` service in a healthy running state.

If the database was removed or not initialized yet, recreate the container and start the stack again:

```powershell
docker compose down -v
docker compose up -d
```

### Ingestion returns `400 Bad Request`

Common causes:
- Empty upload
- Unsupported file type
- PDF with no extractable text
- Text file with no readable content

### Ollama is running but answers are poor or empty

Check that:
- You ingested at least one document first
- The embedding model was pulled successfully
- Ollama is reachable at `http://localhost:11434`
- Your question actually relates to ingested content when using the strict RAG endpoint

## Stop services

```powershell
docker compose down
```

