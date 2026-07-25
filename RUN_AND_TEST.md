# How to Run and Test NotifyRAG

## Current Status

⚠️ **The application is partially implemented**. We have:
- ✅ Database schema (production-grade with dynamic JSONB)
- ✅ Docker Compose configuration
- ✅ Gateway service (complete)
- ⚠️ Core Service (40% complete - needs DTOs, Services, Controllers)
- ⚠️ RAG Service (structure only, needs all Python code)
- ❌ Frontend (not started)

## Prerequisites to Run

### 1. Start Docker Desktop

**Windows:**
- Open Docker Desktop from Start Menu
- Wait for it to fully start (green indicator in system tray)
- Verify: Open PowerShell and run `docker ps`

### 2. Set Environment Variables

```bash
# Copy .env.example to .env
copy .env.example .env

# Edit .env file and add:
CLAUDE_API_KEY=your-actual-claude-api-key-here
JWT_SECRET=your-256-bit-secret-key-here
```

Generate JWT secret:
```bash
# PowerShell
[Convert]::ToBase64String([byte[]](1..32 | ForEach-Object { Get-Random -Maximum 256 }))

# Or online: https://generate-secret.vercel.app/32
```

## What You Can Test NOW (Minimal Setup)

### Option A: Test Database Only

```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Wait 10 seconds for startup
timeout /t 10

# Connect to database
docker exec -it eligibility-gpt-db psql -U eligibility_user -d eligibility_gpt

# Inside psql, verify schema:
\dt core.*;    # Should show: users, user_profiles, notifications, applications
\dt rag.*;     # Should show: document_chunks, posts, eligibility_rules, etc.

# Test pgvector extension
SELECT * FROM pg_extension WHERE extname = 'vector';
```

### Option B: Test What's Implemented

Currently, you can test:
1. ✅ Database schema creation
2. ✅ Gateway routing (if services were running)
3. ❌ Auth endpoints (need to finish Core Service first)
4. ❌ RAG endpoints (need to finish RAG Service first)

## What Needs to Be Completed

### Phase 1: Complete Core Service (Required to Run)

Missing files in `backend/core-service/src/main/java/com/eligibilitygpt/core/`:

**DTOs** (`dto/`):
- LoginRequest.java
- RegisterRequest.java
- AuthResponse.java
- ProfileRequest.java
- ProfileResponse.java
- NotificationResponse.java
- ApplicationResponse.java
- ErrorResponse.java

**Services** (`service/`):
- AuthService.java (register, login, JWT generation)
- UserProfileService.java (CRUD operations)
- NotificationService.java (upload handling)
- ApplicationService.java (tracking)

**Controllers** (`controller/`):
- AuthController.java (@RestController with /api/auth/*)
- UserProfileController.java
- NotificationController.java
- ApplicationController.java
- HealthController.java

**Exception Handling** (`exception/`):
- GlobalExceptionHandler.java
- Custom exception classes

**Estimated Time:** 2-3 hours to complete

### Phase 2: Complete RAG Service (Required to Run)

Missing files in `backend/rag-service/app/`:

**Database** (`db/`):
- database.py (SQLAlchemy setup)
- models.py (ORM models)

**API Routers** (`api/`):
- health.py
- upload.py
- query.py
- eligibility.py

**Services** (`services/`):
- pdf_parser.py
- chunking_service.py
- embedding_service.py
- llm_service.py
- rule_engine.py (THE CRITICAL PIECE)
- retrieval_service.py

**Schemas** (`schemas/`):
- Pydantic models for all requests/responses

**Estimated Time:** 4-6 hours to complete

### Phase 3: Frontend (Optional for Backend Testing)

Create React frontend (not needed for API testing).

**Estimated Time:** 6-8 hours

## How to Complete Implementation

### Quick Path (Recommended)

Use Kiro AI to build each phase:

```
Session 1: "Build Phase 1 from BUILD_PHASES.md - Complete Spring Boot Core Service with all DTOs, Services, and Controllers"

Session 2: "Build Phase 2 from BUILD_PHASES.md - Complete FastAPI RAG Service with database models and core services"

Session 3: "Build Phase 3 from BUILD_PHASES.md - Complete FastAPI API routers and wire everything together"

Session 4: "Test the complete backend with test-api.sh script"
```

### Manual Path

Follow `BUILD_PHASES.md` and implement each file one by one.

## Once Complete - How to Run

### 1. Start All Services

```bash
# Start everything
docker-compose up -d

# Watch logs
docker-compose logs -f
```

### 2. Wait for Services to Be Ready

Services startup time:
- PostgreSQL: ~10 seconds
- Gateway: ~20 seconds
- Core Service: ~30 seconds
- RAG Service: ~60 seconds (first time, downloads models)

Check health:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8000/health
```

### 3. Run Automated Tests

**Linux/Mac:**
```bash
chmod +x test-api.sh
./test-api.sh
```

**Windows:**
```bash
test-api.bat
```

### 4. Manual API Testing

**Register User:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "fullName": "Test User"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'
```

Save the token from response, then:

**Get Profile:**
```bash
curl -X GET http://localhost:8080/api/profiles/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Testing Strategy

### Unit Tests (Not Yet Implemented)

```bash
# Core Service
cd backend/core-service
mvn test

# RAG Service
cd backend/rag-service
pytest
```

### Integration Tests (Not Yet Implemented)

Would test:
1. Auth flow (register → login → access protected endpoint)
2. Profile CRUD (create → read → update)
3. PDF upload → processing → query
4. Eligibility check with rule engine

### End-to-End Tests

Once complete, test full workflow:
1. Register user
2. Upload SSC CGL PDF
3. Wait for processing
4. Update user profile (DOB, category, education)
5. Ask question: "What is the age limit for General category?"
6. Check eligibility for specific post
7. Batch scan all posts
8. Mark post as interested

## Troubleshooting

### "Connection refused" errors
- Check if Docker Desktop is running
- Check if containers are up: `docker-compose ps`
- Check logs: `docker-compose logs [service-name]`

### "401 Unauthorized"
- Check if JWT token is valid
- Check if token is in Authorization header: `Bearer YOUR_TOKEN`

### "500 Internal Server Error"
- Check service logs: `docker-compose logs core-service`
- Check database connection
- Check if all required environment variables are set

### Database connection errors
- Verify PostgreSQL is running: `docker-compose ps postgres`
- Check credentials in .env file
- Restart PostgreSQL: `docker-compose restart postgres`

## Next Steps

**To actually run and test the application, you need to:**

1. ✅ Complete Phase 1 (Core Service) - use Kiro or manual implementation
2. ✅ Complete Phase 2 (RAG Service) - use Kiro or manual implementation
3. ✅ Start Docker Desktop
4. ✅ Set environment variables (.env file)
5. ✅ Run `docker-compose up -d`
6. ✅ Execute test script: `test-api.sh` or `test-api.bat`

**Estimated time to fully working backend:** 6-9 hours
**Estimated time with Kiro AI assistance:** 2-3 hours

## Current Implementation Progress

```
├── backend/
│   ├── gateway/                 ✅ 100% Complete
│   ├── core-service/            ⚠️  40% Complete
│   │   ├── models/              ✅ Done (User, UserProfile, Notification, Application)
│   │   ├── repositories/        ✅ Done (all 4 repositories)
│   │   ├── security/            ✅ Done (JWT, filters, config)
│   │   ├── dto/                 ❌ TODO (8 files)
│   │   ├── service/             ❌ TODO (4 files)
│   │   ├── controller/          ❌ TODO (4 files)
│   │   └── exception/           ❌ TODO (2 files)
│   │
│   ├── rag-service/             ⚠️  10% Complete
│   │   ├── config.py            ✅ Done
│   │   ├── main.py              ✅ Done (skeleton)
│   │   ├── db/                  ❌ TODO (2 files)
│   │   ├── schemas/             ❌ TODO (5 files)
│   │   ├── services/            ❌ TODO (6 files)
│   │   └── api/                 ❌ TODO (4 files)
│   │
│   └── db-init/                 ✅ 100% Complete (production schema)
│
├── frontend/                    ❌ 0% Complete
│
├── docker-compose.yml           ✅ Complete
├── test-api.sh                  ✅ Complete
└── test-api.bat                 ✅ Complete
```

**Bottom Line:** The foundation is solid, but implementation is needed before we can run and test.
