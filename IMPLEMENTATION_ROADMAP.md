# Implementation Roadmap

Detailed step-by-step guide for completing ElgibilityGPT from current state to working prototype.

## Current Status: Foundation Complete ✅

All infrastructure, configuration, and architectural decisions are in place. Time to implement the actual business logic!

---

## PHASE 1: Core Service Implementation (Week 1, Days 1-3)

### Priority 1.1: Entity Models (2-3 hours)

**Location:** `backend/core-service/src/main/java/com/eligibilitygpt/core/model/`

**Files to create:**
1. `User.java` - User account entity
2. `UserProfile.java` - User profile with eligibility fields
3. `JobPosting.java` - Notification metadata
4. `UserApplication.java` - Posts user marked for application
5. `SessionProfile.java` - Temporary session-scoped profiles

**Key considerations:**
- Use Lombok `@Data`, `@Entity`, `@Table(schema = "core")`
- Add proper JPA relationships (`@OneToOne`, `@OneToMany`)
- Include validation annotations (`@NotNull`, `@Email`, etc.)
- Add JSON handling for JSONB columns (Hypersistence Utils)

**Example structure:**
```java
@Data
@Entity
@Table(name = "users", schema = "core")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    // ... other fields
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;
}
```

### Priority 1.2: Repository Layer (1 hour)

**Location:** `backend/core-service/src/main/java/com/eligibilitygpt/core/repository/`

**Files to create:**
1. `UserRepository.java`
2. `UserProfileRepository.java`
3. `JobPostingRepository.java`
4. `UserApplicationRepository.java`
5. `SessionProfileRepository.java`

**Example:**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### Priority 1.3: DTOs (Data Transfer Objects) (2 hours)

**Location:** `backend/core-service/src/main/java/com/eligibilitygpt/core/dto/`

**Create request/response DTOs for:**
- Auth (LoginRequest, RegisterRequest, AuthResponse)
- Profile (ProfileRequest, ProfileResponse)
- JobPosting (JobPostingRequest, JobPostingResponse)
- Application (ApplicationRequest, ApplicationResponse)

### Priority 1.4: JWT Security Configuration (3-4 hours)

**Location:** `backend/core-service/src/main/java/com/eligibilitygpt/core/security/`

**Files to create:**
1. `JwtUtil.java` - Generate and validate JWT tokens
2. `JwtAuthenticationFilter.java` - Filter to intercept and validate requests
3. `SecurityConfig.java` - Spring Security configuration
4. `UserDetailsServiceImpl.java` - Load user for authentication

**Key endpoints to secure:**
- Public: `/api/auth/login`, `/api/auth/register`
- Authenticated: Everything else

### Priority 1.5: Service Layer (4-5 hours)

**Location:** `backend/core-service/src/main/java/com/eligibilitygpt/core/service/`

**Files to create:**
1. `AuthService.java` - User registration, login
2. `UserProfileService.java` - Profile CRUD, validation
3. `JobPostingService.java` - Job posting metadata management
4. `UserApplicationService.java` - Application tracking
5. `RagServiceClient.java` - WebClient for calling RAG service

**Example business logic:**
```java
@Service
public class UserProfileService {
    public ProfileResponse updateProfile(Long userId, ProfileRequest request) {
        // Validate age (DOB not in future)
        // Validate category (enum check)
        // Save to database
        // Return response
    }
    
    public List<String> getMissingFields(UserProfile profile, Set<String> requiredFields) {
        // Compare profile fields against required fields
        // Return list of missing fields
    }
}
```

### Priority 1.6: REST Controllers (3-4 hours)

**Location:** `backend/core-service/src/main/java/com/eligibilitygpt/core/controller/`

**Files to create:**
1. `AuthController.java` - `/api/auth/*`
2. `UserProfileController.java` - `/api/profiles/*`
3. `JobPostingController.java` - `/api/job-postings/*`
4. `UserApplicationController.java` - `/api/applications/*`

**Example:**
```java
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService profileService;
    
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        // Get profile for authenticated user
    }
    
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        // Update profile
    }
}
```

### Priority 1.7: Exception Handling (1-2 hours)

**Location:** `backend/core-service/src/main/java/com/eligibilitygpt/core/exception/`

**Files to create:**
1. `GlobalExceptionHandler.java` - `@ControllerAdvice`
2. Custom exceptions (e.g., `UserNotFoundException`, `InvalidProfileException`)

### Priority 1.8: Dockerfile (30 minutes)

Create `backend/core-service/Dockerfile` (similar to gateway Dockerfile)

---

## PHASE 2: RAG Service Implementation (Week 1-2, Days 4-7)

### Priority 2.1: Database Setup (2 hours)

**Location:** `backend/rag-service/app/db/`

**Files to create:**
1. `database.py` - SQLAlchemy engine and session management
2. `models.py` - SQLAlchemy ORM models for RAG schema tables

**Example:**
```python
from sqlalchemy import Column, Integer, String, Text, ARRAY, Float, TIMESTAMP
from sqlalchemy.dialects.postgresql import JSONB
from pgvector.sqlalchemy import Vector

class DocumentChunk(Base):
    __tablename__ = 'document_chunks'
    __table_args__ = {'schema': 'rag'}
    
    id = Column(Integer, primary_key=True)
    job_posting_id = Column(Integer, nullable=False)
    chunk_text = Column(Text, nullable=False)
    embedding = Column(Vector(1024))  # BGE-large dimension
    page_number = Column(Integer)
    # ... other fields
```

### Priority 2.2: Pydantic Schemas (2 hours)

**Location:** `backend/rag-service/app/schemas/`

**Files to create:**
1. `upload.py` - PDF upload request/response schemas
2. `query.py` - Q&A request/response schemas
3. `eligibility.py` - Eligibility check schemas
4. `extraction.py` - Extracted eligibility data schema

**Example:**
```python
from pydantic import BaseModel
from typing import List, Optional
from datetime import date

class EligibilityCheckRequest(BaseModel):
    job_posting_id: int
    post_code: Optional[str] = None
    user_profile: UserProfileData

class UserProfileData(BaseModel):
    date_of_birth: date
    category: str  # GENERAL, OBC, SC, ST, EWS
    gender: str
    education_level: str
    # ... other fields
```

### Priority 2.3: PDF Parsing Service (6-8 hours)

**Location:** `backend/rag-service/app/services/pdf_parser.py`

**Key responsibilities:**
1. Extract text + tables from PDF (PyMuPDF + pdfplumber)
2. Detect document structure (headings, sections)
3. Identify post boundaries
4. Handle OCR fallback for scanned pages
5. Extract metadata (total pages, notification number, etc.)

**Critical functions:**
```python
class PDFParser:
    def parse_pdf(self, file_path: str) -> ParsedDocument:
        # Extract all pages
        # Detect sections using layout + LLM classifier
        # Extract tables separately
        # Return structured document
        
    def detect_sections(self, pages: List[Page]) -> List[Section]:
        # Use font size, bold, all-caps patterns
        # Call LLM to classify semantic section type
        # Return section boundaries
```

### Priority 2.4: Chunking Service (4-6 hours)

**Location:** `backend/rag-service/app/services/chunking_service.py`

**Key responsibilities:**
1. Hierarchical, section-aware chunking
2. Keep post eligibility criteria intact (don't split mid-post)
3. Add overlap only at semantic boundaries
4. Tag chunks with metadata (section_type, post_code, page_number)

**Critical functions:**
```python
class ChunkingService:
    def chunk_document(self, parsed_doc: ParsedDocument) -> List[DocumentChunk]:
        # Level 1: Split by major sections
        # Level 2: Split eligibility section by post
        # Level 3: Split large posts into sub-chunks (keep semantic units)
        # Add metadata to each chunk
```

### Priority 2.5: Embedding Service (3-4 hours)

**Location:** `backend/rag-service/app/services/embedding_service.py`

**Key responsibilities:**
1. Load sentence-transformers model (BGE-large-en-v1.5)
2. Batch embed chunks
3. Store embeddings in pgvector
4. Handle initialization and caching

**Critical functions:**
```python
class EmbeddingService:
    def __init__(self):
        self.model = SentenceTransformer('BAAI/bge-large-en-v1.5')
    
    async def embed_chunks(self, chunks: List[str]) -> np.ndarray:
        # Batch process chunks
        # Return embeddings array
    
    async def embed_query(self, query: str) -> np.ndarray:
        # Embed single query
```

### Priority 2.6: Retrieval Service (5-6 hours)

**Location:** `backend/rag-service/app/services/retrieval_service.py`

**Key responsibilities:**
1. Hybrid retrieval (dense vector + BM25 keyword)
2. Filter by job_posting_id, section_type, post_code
3. Rerank top-k results with cross-encoder
4. Return grounded context with source metadata

**Critical functions:**
```python
class RetrievalService:
    async def hybrid_search(
        self, 
        query: str, 
        job_posting_id: int,
        top_k: int = 20,
        filters: Optional[Dict] = None
    ) -> List[RetrievedChunk]:
        # Dense vector search (pgvector cosine similarity)
        # BM25 keyword search
        # Merge results with weighted score
        # Rerank top-k with cross-encoder
        # Return with source metadata
```

### Priority 2.7: LLM Service (4-5 hours)

**Location:** `backend/rag-service/app/services/llm_service.py`

**Key responsibilities:**
1. Claude API client with retry logic
2. Prompt templates for extraction and Q&A
3. Structured output parsing (JSON extraction)
4. Token counting and rate limiting

**Critical functions:**
```python
class LLMService:
    async def extract_eligibility(
        self, 
        post_section: str, 
        post_name: str
    ) -> ExtractedEligibility:
        # Prompt: extract age, education, relaxations
        # Parse JSON response
        # Validate against schema
        
    async def answer_question(
        self, 
        question: str, 
        context: List[RetrievedChunk]
    ) -> Answer:
        # Prompt: answer using only provided context
        # Include page citations
        # Return confidence score
```

### Priority 2.8: Eligibility Rule Engine (6-8 hours)

**Location:** `backend/rag-service/app/services/rule_engine.py`

**Key responsibilities:**
1. **Age calculation** - compute age as of specific date, handle leap years
2. **Category relaxation** - apply age relaxation rules per category
3. **Education matching** - match user education against requirements
4. **Deterministic verdict** - no LLM math, pure Python logic

**Critical functions:**
```python
class RuleEngine:
    def check_age_eligibility(
        self, 
        dob: date, 
        min_age: int, 
        max_age: int, 
        as_on_date: date,
        category: str,
        relaxations: Dict[str, int]
    ) -> AgeCheckResult:
        # Calculate exact age
        # Apply category relaxation
        # Return pass/fail with details
        
    def check_education_eligibility(
        self,
        user_education: str,
        required_education: List[str]
    ) -> EducationCheckResult:
        # Match degree level and specialization
        # Handle "or equivalent" clauses
        
    def compute_verdict(
        self,
        post: PostEligibility,
        profile: UserProfile
    ) -> EligibilityVerdict:
        # Run all checks
        # Generate natural language verdict
        # Cite relevant clauses
```

### Priority 2.9: API Routers (4-5 hours)

**Location:** `backend/rag-service/app/api/`

**Files to create:**
1. `health.py` - Health check endpoint
2. `upload.py` - PDF upload and processing endpoints
3. `query.py` - Q&A endpoints
4. `eligibility.py` - Eligibility check endpoints

**Example:**
```python
@router.post("/upload/pdf")
async def upload_pdf(
    file: UploadFile = File(...),
    metadata: str = Form(...),
    background_tasks: BackgroundTasks
):
    # Save file
    # Create job_posting record in core service
    # Start background processing
    # Return job_posting_id and status
    
@router.post("/eligibility/check")
async def check_eligibility(request: EligibilityCheckRequest):
    # Load post_eligibility from database
    # Run rule engine
    # Generate verdict with LLM
    # Return structured response
```

### Priority 2.10: Dockerfile (30 minutes)

Create `backend/rag-service/Dockerfile`

---

## PHASE 3: Integration & Testing (Week 2, Days 1-2)

### Priority 3.1: End-to-End Flow Test

1. Start all services (Docker Compose)
2. Register user via Core Service
3. Create profile
4. Upload PDF via RAG Service
5. Wait for processing
6. Ask Q&A question
7. Check eligibility for a post
8. Verify all responses

### Priority 3.2: Fix Integration Issues

- JWT token passing between services
- CORS configuration
- Database schema mismatches
- Service discovery issues

---

## PHASE 4: Frontend Implementation (Week 2-3, Days 3-7)

### Priority 4.1: Project Setup (1-2 hours)

```bash
cd frontend
npm create vite@latest . -- --template react
npm install react-router-dom axios @tanstack/react-query
npm install tailwindcss postcss autoprefixer framer-motion
npm install lucide-react react-hot-toast
```

### Priority 4.2: Core Components (8-10 hours)

**Location:** `frontend/src/`

**Key pages:**
1. `pages/Login.jsx` - Login/register form
2. `pages/Profile.jsx` - User profile form
3. `pages/Upload.jsx` - PDF upload with progress
4. `pages/Chat.jsx` - Q&A interface
5. `pages/Eligibility.jsx` - Eligibility results display
6. `pages/Applications.jsx` - Saved applications dashboard

**Key components:**
1. `components/Navbar.jsx`
2. `components/ProfileForm.jsx`
3. `components/ChatMessage.jsx`
4. `components/EligibilityCard.jsx`
5. `components/PDFUploader.jsx`

### Priority 4.3: API Client (2-3 hours)

**Location:** `frontend/src/services/api.js`

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authAPI = {
  login: (credentials) => api.post('/api/auth/login', credentials),
  register: (data) => api.post('/api/auth/register', data),
};

export const profileAPI = {
  get: () => api.get('/api/profiles/me'),
  update: (data) => api.put('/api/profiles/me', data),
};

// ... other API methods
```

### Priority 4.4: State Management (2-3 hours)

**Location:** `frontend/src/context/`

**Files:**
1. `AuthContext.jsx` - User authentication state
2. `ProfileContext.jsx` - User profile state

### Priority 4.5: Styling (3-4 hours)

- Setup Tailwind CSS
- Create consistent design system
- Add Framer Motion animations
- Responsive design for mobile

### Priority 4.6: Dockerfile (30 minutes)

Create `frontend/Dockerfile` (multi-stage build with nginx)

---

## PHASE 5: Polish & Demo Preparation (Week 3-4)

### Priority 5.1: Demo Data (3-4 hours)

- Find or create a realistic SSC CGL-style notification PDF
- Manually label 5-10 posts for accuracy testing
- Create test user profiles

### Priority 5.2: Error Handling & UX (4-5 hours)

- Loading states for all async operations
- Error messages for API failures
- Validation feedback on forms
- Empty states (no applications, no PDFs)

### Priority 5.3: Documentation (2-3 hours)

- Add inline code comments
- Create DEMO_GUIDE.md with walkthrough
- Record demo video (optional)
- Update README with screenshots

### Priority 5.4: Performance Testing (2-3 hours)

- Test with 100+ page PDF
- Measure query response times
- Optimize slow queries
- Add database indexes if needed

---

## Estimated Timeline Summary

| Phase | Duration | Effort |
|-------|----------|--------|
| Phase 1: Core Service | 2-3 days | 20-25 hours |
| Phase 2: RAG Service | 4-5 days | 35-45 hours |
| Phase 3: Integration | 1-2 days | 8-12 hours |
| Phase 4: Frontend | 3-4 days | 25-30 hours |
| Phase 5: Polish | 2-3 days | 12-16 hours |
| **Total** | **12-17 days** | **100-128 hours** |

## Development Best Practices

1. **Commit frequently** - After each working feature
2. **Test as you go** - Don't wait until the end
3. **Use logging extensively** - Especially in RAG pipeline
4. **Start simple** - Get basic flow working before optimizing
5. **Ask for help** - Complex LLM prompts may need iteration

## Tools to Speed Up Development

- **GitHub Copilot** - For boilerplate code
- **Postman** - For API testing
- **pgAdmin** - For database inspection
- **Docker Desktop** - For service management
- **VS Code** - With Spring Boot and Python extensions

## When to Iterate vs Move On

**Iterate if:**
- Core flow is broken (can't upload PDF, can't check eligibility)
- Accuracy is <80% on test cases
- Services crash or hang frequently

**Move on if:**
- Minor UI polish issues
- Edge cases in PDF parsing (can handle manually for demo)
- Advanced features (multi-language, OCR, etc.)

---

**Remember:** The goal is a working prototype/demo, not production-ready software. Focus on the happy path first, then add error handling and edge cases.

Good luck! 🚀
