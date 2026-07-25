# ElgibilityGPT Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                                    │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────┐        │
│  │  React Frontend (Port 3000)                                 │        │
│  │  - React 18 + Vite + React Router                          │        │
│  │  - Tailwind CSS + Framer Motion                            │        │
│  │  - Axios + React Query                                      │        │
│  └────────────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓ HTTP/REST
┌─────────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY LAYER                               │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────┐        │
│  │  Spring Cloud Gateway (Port 8080)                          │        │
│  │  - Route management                                         │        │
│  │  - Rate limiting (100 req/s)                               │        │
│  │  - Circuit breaker (Resilience4j)                          │        │
│  │  - CORS configuration                                       │        │
│  └────────────────────────────────────────────────────────────┘        │
└─────────────────────────────────────────────────────────────────────────┘
                    ↓                                     ↓
        ┌──────────────────────┐             ┌──────────────────────┐
        │ /api/auth/**         │             │ /api/rag/**          │
        │ /api/users/**        │             │                      │
        │ /api/profiles/**     │             │                      │
        │ /api/job-postings/** │             │                      │
        │ /api/applications/** │             │                      │
        └──────────────────────┘             └──────────────────────┘
                    ↓                                     ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                       APPLICATION SERVICES LAYER                         │
│                                                                          │
│  ┌──────────────────────────────┐    ┌──────────────────────────────┐ │
│  │  Core Service (Port 8081)     │    │  RAG Service (Port 8000)      │ │
│  │  Spring Boot 3.2              │    │  FastAPI 0.109               │ │
│  │                               │    │                               │ │
│  │  Components:                  │    │  Components:                  │ │
│  │  ├─ Auth & JWT               │    │  ├─ PDF Parser                │ │
│  │  ├─ User Management           │    │  ├─ Chunking Service         │ │
│  │  ├─ Profile CRUD              │    │  ├─ Embedding Service        │ │
│  │  ├─ Job Posting Metadata     │    │  ├─ Retrieval Service        │ │
│  │  ├─ Application Tracking     │    │  ├─ LLM Service (Claude)     │ │
│  │  └─ WebClient to RAG         │    │  └─ Rule Engine              │ │
│  │                               │◄──►│                               │ │
│  │  Technology:                  │REST│  Technology:                  │ │
│  │  ├─ Spring Data JPA          │    │  ├─ SQLAlchemy                │ │
│  │  ├─ Spring Security           │    │  ├─ PyMuPDF + pdfplumber     │ │
│  │  ├─ Hibernate                 │    │  ├─ sentence-transformers    │ │
│  │  └─ JJWT                      │    │  ├─ Anthropic Claude API     │ │
│  └──────────────────────────────┘    │  └─ rank-bm25                 │ │
│                                        └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
            ↓                                           ↓
┌─────────────────────────────────────────────────────────────────────────┐
│                       DATA PERSISTENCE LAYER                             │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │  PostgreSQL 16 + pgvector (Port 5432)                            │  │
│  │                                                                   │  │
│  │  Schema: core                    Schema: rag                      │  │
│  │  ├─ users                        ├─ document_chunks              │  │
│  │  ├─ user_profiles                │  └─ embedding (vector[1024])  │  │
│  │  ├─ job_postings                 ├─ post_eligibility             │  │
│  │  ├─ user_applications            ├─ document_processing_log      │  │
│  │  └─ session_profiles             └─ eligibility_check_cache      │  │
│  │                                                                   │  │
│  │  Indexes:                                                         │  │
│  │  ├─ B-tree indexes on PKs, FKs, common queries                  │  │
│  │  └─ HNSW index on embeddings (vector_cosine_ops)                │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

## Service Communication Patterns

### 1. User Authentication Flow
```
Client → Gateway → Core Service → PostgreSQL (core.users)
                     ↓
                  Generate JWT
                     ↓
Client ← Gateway ← JWT Token
```

### 2. PDF Upload & Processing Flow
```
Client → Gateway → RAG Service
                      ↓
                   Save PDF to disk
                      ↓
                   Parse PDF (PyMuPDF)
                      ↓
                   Detect sections (LLM)
                      ↓
                   Chunk document
                      ↓
                   Generate embeddings (BGE-large)
                      ↓
                   Store in pgvector
                      ↓
                   Extract eligibility (LLM)
                      ↓
                   Store structured data
                      ↓
Client ← Gateway ← Processing complete
```

### 3. Eligibility Check Flow
```
Client → Gateway → RAG Service
                      ↓
                   Load post_eligibility from DB
                      ↓
                   Run rule engine (deterministic)
                      ├─ Age check
                      ├─ Education check
                      ├─ Category check
                      └─ Generate verdict
                      ↓
                   Call LLM for natural language verdict
                      ↓
Client ← Gateway ← Eligibility result with citations
```

### 4. Q&A Flow
```
Client → Gateway → RAG Service
                      ↓
                   Embed query (BGE-large)
                      ↓
                   Hybrid retrieval
                      ├─ Dense search (pgvector cosine)
                      └─ Sparse search (BM25)
                      ↓
                   Merge & rerank (cross-encoder)
                      ↓
                   Call LLM with context
                      ↓
                   Generate grounded answer
                      ↓
Client ← Gateway ← Answer with page citations
```

## Data Flow: PDF → Structured Eligibility

```
┌────────────────────┐
│  Raw PDF (125 pgs) │
└──────────┬─────────┘
           ↓
    ┌─────────────┐
    │ PDF Parser  │ → Extract text, tables, metadata
    └──────┬──────┘
           ↓
 ┌─────────────────┐
 │ Section Detector │ → Identify: Vacancy, Eligibility, Fee, Dates, Annexures
 └────────┬─────────┘
           ↓
    ┌──────────────┐
    │   Chunker    │ → Hierarchical, section-aware chunks (512 tokens)
    └──────┬───────┘
           ↓
 ┌─────────────────┐
 │ Embedding Model │ → BGE-large-en-v1.5 (1024 dim)
 └────────┬─────────┘
           ↓
    ┌──────────────┐
    │  pgvector    │ → Store embeddings + metadata
    └──────────────┘
           
           ↓ (parallel)
           
 ┌─────────────────────────┐
 │ LLM Extraction (Claude) │ → Extract per-post eligibility
 └────────┬────────────────┘
           ↓
    ┌──────────────────────┐
    │ Structured JSON      │ → age_limit, education, relaxations
    │ {                     │
    │   post_name: "...",   │
    │   min_age: 18,        │
    │   max_age: 30,        │
    │   age_relax_obc: 3,   │
    │   education: [...]    │
    │ }                     │
    └──────┬────────────────┘
           ↓
 ┌──────────────────────┐
 │ post_eligibility tbl │ → Store for rule engine
 └──────────────────────┘
```

## Key Architectural Decisions

### 1. Why Polyglot Microservices?

**Java Spring Boot for Core:**
- Mature ecosystem for enterprise patterns (auth, transactions, security)
- Strong typing for business logic reduces bugs
- Demonstrates Full Stack Java skills
- Native JDBC/JPA for complex relational queries

**Python FastAPI for RAG/ML:**
- Rich ML/NLP ecosystem (PyTorch, Hugging Face, sentence-transformers)
- Rapid prototyping for research-oriented code
- Native async for I/O-bound tasks (embedding, LLM calls)
- Community libraries for PDF, chunking, retrieval

### 2. Why Hybrid Eligibility (Structured + RAG)?

**Problem:** Pure RAG Q&A is unreliable for deterministic logic.
- LLMs can miscalculate age ("25 years on 2026-01-01")
- Relaxation rules are numerical (3 years for OBC) - can't afford approximation
- Edge cases (leap years, "as on date") need exact logic

**Solution:** LLM extracts rules once → Python rule engine applies them
- **LLM strength:** Understand natural language rules from PDFs
- **Code strength:** Deterministic arithmetic, no hallucination risk
- Best of both worlds: semantic understanding + computational accuracy

### 3. Why Notification-Agnostic Pipeline?

**Problem:** SSC, RRB, IBPS, UPSC use different document structures.
- SSC: "ANNEXURE-I", "ANNEXURE-II"
- RRB: "Schedule A", "Schedule B"
- IBPS: No annexures, inline tables
- State PSCs: Vary wildly

**Solution:** Generic section detection + LLM semantic labeling
- Don't hardcode "ANNEXURE" → detect semantically ("this is an eligibility section")
- LLM classifies: Vacancy, Eligibility, Fee, Dates, Annexure, Other
- Schema is exam-agnostic (same fields for all bodies, null if not found)

### 4. Why Hierarchical Chunking?

**Problem:** Naive fixed-token chunking breaks semantic units.
- A post's age limit may be on page 12
- Its category relaxation on page 45
- Its education requirement in a footnote on page 98
- Fixed 512-token chunks will split these apart

**Solution:** Section-aware, post-level chunking
- Detect post boundaries (e.g., "POST-001: Income Tax Inspector")
- Keep all eligibility criteria for one post in one chunk (or tightly linked chunks)
- Add rich metadata (post_code, section_type, page_number) for filtering

### 5. Why pgvector (not standalone FAISS/Qdrant)?

**Reasons:**
- Already using PostgreSQL for relational data
- Simplifies deployment (one DB, not DB + vector store)
- Atomic transactions across relational + vector data
- Mature backup/replication story
- HNSW index is competitive with FAISS for <10M vectors

**Trade-off:**
- Slightly slower than specialized stores at massive scale
- Fine for prototype (10K-100K chunks per notification)

## Scalability Considerations

### Current Architecture (Prototype)
- **Target:** Single user, single PDF processing at a time
- **Max PDF size:** 50 MB, ~200 pages
- **Max concurrent users:** ~10-20
- **Query latency:** <8s for Q&A, <3s for eligibility check

### Production Scale (Future)
- **Horizontal scaling:** Add more RAG service pods (stateless)
- **Async processing:** Use job queue (Celery/Redis) for PDF ingestion
- **Caching:** Redis for frequent queries, eligibility cache table
- **Read replicas:** PostgreSQL read replicas for retrieval
- **CDN:** Static assets, pre-computed results
- **Load balancer:** Nginx/HAProxy in front of gateway

## Security Architecture

### Authentication & Authorization
```
Client → Gateway → JWT validation
                     ↓
              Extract user_id from token
                     ↓
              Pass to Core Service
                     ↓
              Core Service validates permissions
```

- JWT issued by Core Service on login
- Gateway validates JWT signature (shared secret)
- Core Service authorizes resource access (user can only access own profile)
- RAG Service trusts Core Service (no independent auth)

### Data Security
- Passwords hashed with bcrypt (Spring Security default)
- JWT secret must be 256-bit, stored in env variable
- HTTPS/TLS in production
- Rate limiting at Gateway (prevents DoS)
- SQL injection prevention (JPA/SQLAlchemy parameterized queries)
- File upload validation (PDF mime type, size limit)

### Privacy
- PII (category, DOB, disability) stored in `core.user_profiles`
- Session profiles expire after 24 hours (for anonymous users)
- No logging of sensitive fields
- No third-party analytics in prototype
- Explicit disclaimer: not a legal determination

## Technology Stack Versions

| Component | Technology | Version | Reason |
|-----------|-----------|---------|--------|
| Core Service | Spring Boot | 3.2.1 | Latest stable, Java 17 support |
| Core Service | Java | 17 | LTS, modern features |
| Gateway | Spring Cloud Gateway | 2023.0.0 | Reactive, non-blocking |
| RAG Service | Python | 3.10+ | Stable, good library support |
| RAG Service | FastAPI | 0.109 | Fast, async, auto docs |
| Database | PostgreSQL | 16 | Latest, better performance |
| Vector Extension | pgvector | 0.6+ | HNSW index support |
| Embeddings | BGE-large-en-v1.5 | Latest | State-of-art English embeddings |
| LLM | Claude Sonnet 4.5 | Latest | Best reasoning, long context |
| Frontend | React | 18 | Modern hooks, concurrent rendering |
| Frontend | Vite | 5 | Fast HMR, modern build tool |

## Development Environment

### Required Ports
- **3000** - React frontend (dev server)
- **8080** - Spring Cloud Gateway
- **8081** - Spring Boot Core Service
- **8000** - FastAPI RAG Service
- **5432** - PostgreSQL + pgvector

### Resource Requirements
- **RAM:** 8GB minimum, 16GB recommended
- **Disk:** 10GB (includes model cache)
- **CPU:** 4 cores minimum (embedding models are CPU-intensive without GPU)

### Optional GPU Acceleration
- If available, install PyTorch with CUDA support
- Speeds up embedding generation 10-50x
- Not required for prototype

## Monitoring & Observability (Future)

### Metrics to Track
- PDF processing time per page
- Embedding generation time
- LLM latency (extraction, Q&A)
- Retrieval latency
- Rule engine execution time
- Database query performance

### Logging Strategy
- Structured JSON logging
- Correlation IDs across services
- Log levels: ERROR (alert), WARN (review), INFO (audit trail), DEBUG (dev)

### Health Checks
- Gateway: `/actuator/health`
- Core Service: `/actuator/health`
- RAG Service: `/health`
- PostgreSQL: `pg_isready`

## Deployment Options

### 1. Docker Compose (Development)
✅ Current setup
- All services in one compose file
- Shared network
- Volume mounts for development

### 2. Kubernetes (Production)
- Separate deployments per service
- Horizontal pod autoscaling for RAG service
- Persistent volume claims for PostgreSQL
- Ingress for gateway
- ConfigMaps for configuration

### 3. Cloud-Managed (AWS/Azure/GCP)
- RDS/Cloud SQL for PostgreSQL
- ECS/AKS/GKE for containers
- S3/Blob Storage for PDFs
- API Gateway for routing
- Lambda/Functions for async processing

## Future Enhancements

### Phase 2 (v0.2)
- Multi-language support (Hindi)
- OCR for scanned PDFs
- WebSocket for real-time processing updates
- Admin dashboard for monitoring
- Batch upload (multiple PDFs)

### Phase 3 (v0.3)
- Multi-document cross-comparison
- Historical trend analysis
- Email/SMS notifications for application deadlines
- Mobile app (React Native)
- Voice interface integration

### Research Features
- Fine-tuned embedding model on govt notifications
- Few-shot learning for extraction
- Graph-based entity linking (posts → departments → exam centers)
- Automatic update detection (notification amendments)

---

**Last Updated:** July 25, 2026  
**Architecture Version:** 0.1.0
