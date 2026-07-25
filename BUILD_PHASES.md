# NotifyRAG Production Build - Phase-by-Phase Implementation

## Current Status ✅

**Foundation Complete:**
- ✅ Dynamic JSONB database schema (production-grade, exam-agnostic)
- ✅ Docker Compose infrastructure
- ✅ Service structure (Gateway, Core, RAG)
- ✅ Initial entity models (User, UserProfile, Notification, Application)
- ✅ Documentation suite

## Build Order - Feed to Kiro Phase by Phase

### PHASE 1: Complete Spring Boot Core Service (3-4 hours)
**Goal:** Production-ready auth, profile, notification, application modules

```
Create all remaining files for backend/core-service:

1. **Repositories** (backend/core-service/src/main/java/com/eligibilitygpt/core/repository/)
   - UserProfileRepository.java
   - NotificationRepository.java
   - ApplicationRepository.java
   - SessionProfileRepository.java

2. **DTOs** (backend/core-service/src/main/java/com/eligibilitygpt/core/dto/)
   Auth:
   - LoginRequest.java
   - RegisterRequest.java
   - AuthResponse.java
   - TokenRefreshRequest.java
   
   Profile:
   - ProfileRequest.java
   - ProfileResponse.java
   - ProfilePatchRequest.java (for partial updates)
   
   Notification:
   - NotificationUploadRequest.java
   - NotificationResponse.java
   - NotificationStatusResponse.java
   
   Application:
   - ApplicationRequest.java
   - ApplicationResponse.java
   - ApplicationListResponse.java

3. **Security** (backend/core-service/src/main/java/com/eligibilitygpt/core/security/)
   - JwtUtil.java - Generate/validate JWT with access + refresh tokens
   - JwtAuthenticationFilter.java - Filter to validate JWT on requests
   - SecurityConfig.java - Spring Security config, CORS, CSRF disabled for stateless API
   - CustomUserDetailsService.java - Load user for authentication
   - PasswordEncoderConfig.java - BCrypt bean

4. **Services** (backend/core-service/src/main/java/com/eligibilitygpt/core/service/)
   - AuthService.java - register, login, refresh token logic
   - UserProfileService.java - CRUD + partial update (PATCH) + missing fields detection
   - NotificationService.java - upload metadata, status polling, callback handler from RAG service
   - ApplicationService.java - track user applications, list by status
   - RagServiceClient.java - WebClient to call FastAPI endpoints

5. **Controllers** (backend/core-service/src/main/java/com/eligibilitygpt/core/controller/)
   - AuthController.java - POST /api/auth/register, /login, /refresh
   - UserProfileController.java - GET/PUT/PATCH /api/profiles/me
   - NotificationController.java - POST /api/notifications/upload, GET /api/notifications/{id}/status
   - ApplicationController.java - POST/GET /api/applications

6. **Exception Handling** (backend/core-service/src/main/java/com/eligibilitygpt/core/exception/)
   - GlobalExceptionHandler.java - @ControllerAdvice
   - Custom exceptions: UserNotFoundException, InvalidCredentialsException, NotificationProcessingException

7. **Config** (backend/core-service/src/main/java/com/eligibilitygpt/core/config/)
   - WebClientConfig.java - Bean for RAG service communication
   - OpenApiConfig.java - Swagger/OpenAPI docs
   - CorsConfig.java

8. **Dockerfile** (backend/core-service/Dockerfile)
   - Multi-stage build matching gateway pattern
```

### PHASE 2: Complete FastAPI RAG Service - Database & Core (3-4 hours)
**Goal:** Database models, ingestion pipeline, rule engine foundation

```
Create backend/rag-service complete structure:

1. **Database** (backend/rag-service/app/db/)
   - database.py - SQLAlchemy async engine, session management
   - models.py - SQLAlchemy ORM models:
     * DocumentChunk (with Vector column from pgvector)
     * Post (with JSONB eligibility_schema)
     * EligibilityRule (decomposed rules table)
     * ProcessingLog
     * EligibilityCache

2. **Schemas** (backend/rag-service/app/schemas/)
   - upload.py - PdfUploadRequest, IngestionStatusResponse
   - query.py - QueryRequest, QueryResponse, Citation
   - eligibility.py - EligibilityCheckRequest, EligibilityCheckResponse, BatchScanResponse
   - extraction.py - ExtractedEligibility (the dynamic JSONB shape)
   - common.py - UserProfile, PostSummary

3. **Services - PDF Pipeline** (backend/rag-service/app/services/)
   - pdf_parser.py:
     * parse_pdf() - PyMuPDF + pdfplumber, extract text + tables
     * detect_sections() - LLM classifier for semantic sections
     * identify_post_boundaries() - find where posts start/end
   
   - chunking_service.py:
     * chunk_document() - hierarchical, section-aware
     * chunk_per_post() - keep post eligibility intact
     * add_metadata() - page_number, section_type, post_code
   
   - embedding_service.py:
     * initialize() - load sentence-transformers model
     * embed_chunks() - batch embed, store in pgvector
     * embed_query() - single query embedding

4. **Services - LLM** (backend/rag-service/app/services/)
   - llm_service.py:
     * extract_eligibility_structured() - call Claude with extraction prompt
     * generate_answer() - RAG Q&A with context
     * generate_verdict() - natural language eligibility verdict
     * Prompt templates as constants at top of file

5. **Services - Rule Engine** (backend/rag-service/app/services/)
   - rule_engine.py - THE CRITICAL PIECE:
     ```python
     class RuleEngine:
         def __init__(self):
             self.evaluators = {
                 'age': self.evaluate_age,
                 'education': self.evaluate_education,
                 'category': self.evaluate_category,
                 'gender': self.evaluate_gender,
                 'domicile': self.evaluate_domicile,
                 'custom': self.evaluate_custom
             }
         
         def evaluate_post(self, post, user_profile):
             # Load eligibility_schema JSONB
             # Dispatch to evaluators by rule_type
             # Return EligibilityVerdict
         
         def evaluate_age(self, rule_def: dict, profile: dict):
             # Deterministic date arithmetic
             # Apply relaxations
             # Return pass/fail + reasoning
     ```

6. **Services - Retrieval** (backend/rag-service/app/services/)
   - retrieval_service.py:
     * hybrid_search() - pgvector cosine + BM25 merge
     * rerank() - cross-encoder rerank top-k
     * get_context_for_query() - return chunks with metadata
```

### PHASE 3: FastAPI API Routers (2-3 hours)
**Goal:** Wire up all endpoints

```
Create backend/rag-service/app/api/:

1. health.py - GET /health, GET /api/rag/health
2. upload.py - POST /api/rag/ingest (background task)
3. query.py - POST /api/rag/query (intent classification → RAG or eligibility)
4. eligibility.py:
   - POST /api/rag/eligibility/check (single post)
   - POST /api/rag/eligibility/scan (batch all posts)
   
Each router:
- Pydantic request/response validation
- Error handling with HTTPException
- Logging
- OpenAPI tags
```

### PHASE 4: Frontend Foundation - Design System (3-4 hours)
**Goal:** Production-grade UI primitives, NOT default Tailwind

```
Create frontend/ from scratch:

1. **Initialize**
   npm create vite@latest frontend -- --template react
   cd frontend
   npm install react-router-dom zustand @tanstack/react-query axios
   npm install tailwindcss postcss autoprefixer framer-motion
   npm install @studio-freight/lenis
   npm install lucide-react react-hot-toast
   npm install clsx tailwind-merge

2. **Tailwind Config** (tailwind.config.js)
   CRITICAL: Custom design tokens, NOT default blue/purple
   ```js
   export default {
     theme: {
       extend: {
         colors: {
           // Restrained, confident palette (reference: Linear, Arc, Vercel)
           primary: {
             50: '#fafafa',
             100: '#f4f4f5',
             // ... custom scale
             900: '#18181b',
           },
           accent: {
             // Single accent color, used sparingly
             DEFAULT: '#8b5cf6', // example: purple accent
             light: '#a78bfa',
             dark: '#7c3aed',
           },
         },
         fontFamily: {
           sans: ['Inter', 'system-ui', 'sans-serif'],
           mono: ['JetBrains Mono', 'monospace'],
         },
         animation: {
           // Custom Framer Motion-aligned animations
           'fade-in': 'fadeIn 0.5s ease-out',
           'slide-up': 'slideUp 0.6s ease-out',
         },
       },
     },
     plugins: [],
   }
   ```

3. **Design System** (src/components/ui/)
   Build custom components inspired by Aceternity/Origin UI:
   
   - Button.jsx - variants (primary, ghost, outline), animated hover states
   - Input.jsx - with floating label, validation states
   - Card.jsx - bordered with subtle gradient, glassmorphic variant
   - Modal.jsx - animated with Framer Motion, backdrop blur
   - Toast.jsx - react-hot-toast styled
   - Badge.jsx - status chips for eligible/not-eligible
   - Skeleton.jsx - loading states
   - AnimatedIcon.jsx - wrapper for animate-ui style icon animations
   
   Each component:
   - TypeScript or PropTypes
   - Framer Motion variants
   - Dark mode first
   - No "AI wrapper" aesthetic - real information density

4. **Layout** (src/components/layout/)
   - Navbar.jsx - animated on scroll (Lenis integration)
   - Footer.jsx
   - DashboardLayout.jsx - sidebar + main content
   - LenisWrapper.jsx - smooth scroll wrapper

5. **Lenis Setup** (src/App.jsx)
   ```jsx
   import Lenis from '@studio-freight/lenis'
   
   useEffect(() => {
     const lenis = new Lenis({
       duration: 1.2,
       easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
       smooth: true,
     })
     
     function raf(time) {
       lenis.raf(time)
       requestAnimationFrame(raf)
     }
     requestAnimationFrame(raf)
   }, [])
   ```
```

### PHASE 5: Frontend Pages - Auth & Landing (2-3 hours)
```
1. src/pages/Landing.jsx
   - Hero with animated headline (Framer Motion stagger)
   - Lenis smooth scroll sections
   - Feature cards with hover animations
   - CTA to /upload
   - Reference aesthetic: Linear.app homepage

2. src/pages/Login.jsx & Register.jsx
   - Split-screen layout
   - Animated form validation (animate-ui icons)
   - JWT storage in localStorage
   - Redirect on success

3. src/context/AuthContext.jsx
   - Zustand store or React Context
   - Login/logout/token refresh logic
   - Protected route wrapper

4. src/services/api.js
   - Axios instance with JWT interceptor
   - API methods: authAPI, profileAPI, notificationAPI, etc.
```

### PHASE 6: Frontend Pages - Core Workspace (4-5 hours)
```
1. src/pages/Upload.jsx
   - Drag-and-drop PDF upload (react-dropzone)
   - Animated progress: upload → parsing → indexing → ready
   - List of previous notifications as cards
   - Status polling with React Query

2. src/pages/Profile.jsx
   - Multi-step form (Framer Motion AnimatePresence between steps)
   - DOB picker, category select, education fields
   - Progress indicator
   - PATCH request for partial updates

3. src/pages/Chat.jsx - THE CORE SCREEN
   - Split view:
     * Left panel: chat thread, message bubbles
     * Right panel: PDF viewer (react-pdf or iframe) that jumps to cited page
   - Message bubbles show citation chips (page badges)
   - Animated send button (animate-ui)
   - Streaming response animation (typed-out reveal if no SSE)
   - Citation click → PDF viewer jumps to page

4. src/pages/EligibilityResults.jsx
   - Grid/list of posts with animated status chips
   - Expandable cards showing reasoning per post
   - Filter/sort controls
   - "Add to applications" button

5. src/pages/Applications.jsx
   - Kanban-lite: interested → eligible_confirmed → applied
   - Drag interactions if time permits (dnd-kit)
   - Otherwise: simple list with status badge
```

### PHASE 7: Integration & Polish (2-3 hours)
```
1. Wire Docker Compose end-to-end
   - Test full flow: register → upload PDF → wait for processing → chat → eligibility check

2. Error handling everywhere
   - API error → toast notification
   - Loading states (skeletons, not blank screens)
   - Empty states designed

3. Responsive QA
   - Mobile nav (hamburger menu)
   - Chat workspace stacks on mobile (chat on top, PDF below)
   - Touch interactions

4. Accessibility pass
   - Focus states on all interactive elements
   - Keyboard navigation on chat input, citation chips
   - ARIA labels where needed

5. Performance check
   - React Query caching configured
   - Lazy load components (React.lazy)
   - Image optimization
```

### PHASE 8: Demo Preparation (2-3 hours)
```
1. Seed data
   - Create realistic SSC CGL-style PDF (or use anonymized real one)
   - Manually label 5-10 posts for accuracy testing

2. Smoke test script
   - Register user
   - Upload PDF
   - Wait for processing (5-10 min)
   - Ask questions: "age limit for General?", "application fee?"
   - Check eligibility for 3 posts
   - Batch scan

3. Documentation
   - Update README with screenshots
   - Create DEMO_GUIDE.md walkthrough
   - Record demo video (optional)

4. Final polish
   - Fix any visual glitches
   - Consistent error messages
   - Loading state timing
```

## Estimated Timeline

| Phase | Duration | Cumulative |
|-------|----------|------------|
| Phase 1: Spring Boot Core | 3-4 hours | 3-4 hours |
| Phase 2: FastAPI Core | 3-4 hours | 6-8 hours |
| Phase 3: FastAPI Routers | 2-3 hours | 8-11 hours |
| Phase 4: Frontend Design System | 3-4 hours | 11-15 hours |
| Phase 5: Frontend Auth & Landing | 2-3 hours | 13-18 hours |
| Phase 6: Frontend Core Workspace | 4-5 hours | 17-23 hours |
| Phase 7: Integration & Polish | 2-3 hours | 19-26 hours |
| Phase 8: Demo Preparation | 2-3 hours | 21-29 hours |

**Total: 21-29 hours** for a production-quality prototype.

## How to Use This with Kiro

**Option A:** Feed all at once (if Kiro can handle it):
```
"Build Phase 1 through Phase 8 from BUILD_PHASES.md"
```

**Option B:** Sequential prompts in same session:
```
Session start:
"Build Phase 1 from BUILD_PHASES.md - Complete Spring Boot Core Service"

After Phase 1 complete:
"Build Phase 2 from BUILD_PHASES.md - FastAPI Database & Core Services"

... continue through Phase 8
```

**Option C:** Parallel tracks (if working with team):
- One dev: Phases 1-3 (backend)
- Another dev: Phases 4-6 (frontend)
- Converge: Phases 7-8 (integration)

## Critical Success Factors

✅ **Dynamic JSONB schema** - already done, don't revert to fixed columns
✅ **Custom design system** - NOT default Tailwind blue/purple
✅ **Lenis smooth scroll** - wrap entire app shell
✅ **Framer Motion** - page transitions, not just button hovers
✅ **Rule engine dispatch** - evaluators keyed by rule_type
✅ **No LLM math** - all date/age arithmetic in Python, not LLM
✅ **Grounded answers** - below-threshold similarity → "not found"
✅ **Real information density** - not Lorem Ipsum or placeholder UI

## Reference Aesthetic

**Good:** Linear, Vercel, Raycast, Arc Browser, Stripe docs
**Avoid:** Bootstrap admin, default shadcn theme, "AI wrapper" gradient overload

---

**Ready to build?** Start with Phase 1 and proceed sequentially.
