# ElgibilityGPT Quick Reference

One-page cheat sheet for common tasks and commands.

## 🚀 Quick Start

```bash
# 1. Setup environment
copy .env.example .env
# Edit .env and add CLAUDE_API_KEY

# 2. Start all services
docker-compose up -d

# 3. Check health
curl http://localhost:8080/actuator/health
curl http://localhost:8000/health

# 4. Access services
# Frontend: http://localhost:3000
# API Docs: http://localhost:8000/docs
# Gateway:  http://localhost:8080
```

## 📁 Project Structure

```
eligibility-gpt/
├── backend/
│   ├── gateway/          # Spring Boot Gateway (8080)
│   ├── core-service/     # Spring Boot Core (8081)
│   └── rag-service/      # FastAPI RAG/ML (8000)
├── frontend/             # React + Vite (3000)
├── docker-compose.yml
└── .env
```

## 🔧 Docker Commands

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d rag-service

# View logs
docker-compose logs -f
docker-compose logs -f rag-service

# Restart service
docker-compose restart core-service

# Stop all services
docker-compose down

# Stop and remove volumes (clears DB)
docker-compose down -v

# Rebuild after code changes
docker-compose up -d --build

# Check service status
docker-compose ps
```

## 🗄️ Database Commands

```bash
# Connect to PostgreSQL
docker exec -it eligibility-gpt-db psql -U eligibility_user -d eligibility_gpt

# Inside psql:
\dt core.*          # List core schema tables
\dt rag.*           # List RAG schema tables
\d+ core.users      # Describe users table
SELECT * FROM core.users;
SELECT count(*) FROM rag.document_chunks;

# Backup database
docker exec eligibility-gpt-db pg_dump -U eligibility_user eligibility_gpt > backup.sql

# Restore database
docker exec -i eligibility-gpt-db psql -U eligibility_user eligibility_gpt < backup.sql
```

## 🧪 Testing API Endpoints

### Authentication
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'

# Save token
TOKEN="your-jwt-token-here"
```

### Profile Management
```bash
# Get profile
curl -X GET http://localhost:8080/api/profiles/me \
  -H "Authorization: Bearer $TOKEN"

# Update profile
curl -X PUT http://localhost:8080/api/profiles/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateOfBirth":"2001-07-03",
    "category":"OBC",
    "gender":"MALE",
    "educationLevel":"Bachelor'\''s Degree"
  }'
```

### PDF Upload
```bash
# Upload PDF
curl -X POST http://localhost:8080/api/rag/upload/pdf \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@notification.pdf" \
  -F 'metadata={"notificationTitle":"SSC CGL 2026","issuingBody":"SSC"}'
```

### Q&A Query
```bash
# Ask question
curl -X POST http://localhost:8080/api/rag/query/ask \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jobPostingId":1,
    "question":"What is the age limit for General category?"
  }'
```

### Eligibility Check
```bash
# Check eligibility
curl -X POST http://localhost:8080/api/rag/eligibility/check \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "jobPostingId":1,
    "postCode":"POST-001",
    "userProfile":{
      "dateOfBirth":"2001-07-03",
      "category":"OBC",
      "gender":"MALE",
      "educationLevel":"Bachelor'\''s Degree"
    }
  }'
```

## 🔨 Development Workflow

### Backend (Java)
```bash
# Core Service
cd backend/core-service
mvn clean install
mvn spring-boot:run
mvn test

# Gateway
cd backend/gateway
mvn clean install
mvn spring-boot:run
```

### Backend (Python)
```bash
# RAG Service
cd backend/rag-service
python -m venv venv
venv\Scripts\activate  # Windows
source venv/bin/activate  # Linux/Mac

pip install -r requirements.txt
uvicorn app.main:app --reload

# Run tests
pytest
pytest -v tests/test_pdf_parser.py

# Format code
black .
flake8 .
```

### Frontend
```bash
cd frontend
npm install
npm run dev        # Development server
npm run build      # Production build
npm run preview    # Preview production build
npm test           # Run tests
```

## 🐛 Debugging

### Check Service Health
```bash
# Gateway
curl http://localhost:8080/actuator/health

# Core Service  
curl http://localhost:8081/actuator/health

# RAG Service
curl http://localhost:8000/health
```

### View Service Logs
```bash
# All services
docker-compose logs -f

# Specific service with timestamps
docker-compose logs -f --timestamps rag-service

# Last 100 lines
docker-compose logs --tail=100 core-service
```

### Common Issues

**Port already in use:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <pid> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

**Database connection failed:**
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Restart PostgreSQL
docker-compose restart postgres

# Check connection
docker exec eligibility-gpt-db pg_isready
```

**Out of memory:**
```bash
# Increase Docker memory limit in Docker Desktop settings
# Recommended: 8GB minimum

# Check current usage
docker stats
```

## 📊 Monitoring

### Service URLs
- Frontend: http://localhost:3000
- Gateway: http://localhost:8080
- Core Service: http://localhost:8081
- RAG Service: http://localhost:8000
- API Docs (Swagger): http://localhost:8000/docs
- Gateway Health: http://localhost:8080/actuator/health
- Core Health: http://localhost:8081/actuator/health

### Database Connection
- Host: localhost
- Port: 5432
- Database: eligibility_gpt
- User: eligibility_user
- Password: eligibility_pass

## 🛠️ Useful Snippets

### Reset Everything
```bash
# Stop services, remove volumes, rebuild, start
docker-compose down -v
docker-compose up -d --build
```

### View Database Logs
```bash
docker-compose logs -f postgres
```

### Execute SQL File
```bash
docker exec -i eligibility-gpt-db psql -U eligibility_user -d eligibility_gpt < script.sql
```

### Export Database Schema
```bash
docker exec eligibility-gpt-db pg_dump -U eligibility_user -d eligibility_gpt --schema-only > schema.sql
```

### Hot Reload Services
```bash
# Java (Maven) - automatic with spring-boot-devtools
# Python - automatic with --reload flag
# React - automatic with Vite HMR
```

## 📝 Git Workflow

```bash
# Feature branch
git checkout -b feature/eligibility-rule-engine
# ... make changes ...
git add .
git commit -m "feat: implement age calculation in rule engine"
git push origin feature/eligibility-rule-engine

# Hotfix
git checkout -b hotfix/jwt-expiration
# ... fix ...
git commit -m "fix: correct JWT expiration time"
git push origin hotfix/jwt-expiration
```

## 🔐 Environment Variables

### Required
```bash
CLAUDE_API_KEY=sk-ant-...         # From Anthropic
JWT_SECRET=<256-bit-secret>        # Generate with: openssl rand -base64 32
```

### Optional (have defaults)
```bash
POSTGRES_DB=eligibility_gpt
POSTGRES_USER=eligibility_user
POSTGRES_PASSWORD=eligibility_pass
CORE_SERVICE_URL=http://localhost:8081
RAG_SERVICE_URL=http://localhost:8000
```

## 📚 Documentation Links

- [Full Setup Guide](SETUP.md)
- [API Documentation](API.md)
- [Architecture Details](ARCHITECTURE.md)
- [Implementation Roadmap](IMPLEMENTATION_ROADMAP.md)
- [Project Summary](PROJECT_SUMMARY.md)

## 💡 Pro Tips

1. **Use API docs:** Visit http://localhost:8000/docs for interactive API testing
2. **Watch logs:** Run `docker-compose logs -f` in a separate terminal
3. **Database tool:** Use pgAdmin or DBeaver to visualize database
4. **Postman:** Import API endpoints for easier testing
5. **Hot reload:** Most code changes auto-reload (Java with devtools, Python with --reload, React with HMR)

## ⚡ Performance Tips

- **Embeddings:** First run downloads models (~500MB), subsequent runs are cached
- **PDF parsing:** Large PDFs (100+ pages) take 2-5 minutes to process
- **LLM calls:** Claude API calls take 2-5 seconds each
- **Batch operations:** Process multiple chunks in parallel where possible

## 🆘 Getting Help

1. Check service logs: `docker-compose logs -f [service-name]`
2. Verify environment: Check `.env` file
3. Database state: Connect with psql and inspect tables
4. API errors: Check response body and status code
5. Read documentation: See `SETUP.md` and `API.md`

---

**Quick Reference Version:** 1.0  
**Last Updated:** July 25, 2026
