# ElgibilityGPT Setup Guide

Complete setup instructions for local development and Docker deployment.

## Prerequisites

### Required Software

1. **Java 17+** - For Spring Boot services
   ```bash
   java -version
   ```

2. **Maven 3.8+** - For building Java services
   ```bash
   mvn -version
   ```

3. **Python 3.10+** - For RAG/ML service
   ```bash
   python --version
   ```

4. **Node.js 18+** and **npm** - For React frontend
   ```bash
   node --version
   npm --version
   ```

5. **PostgreSQL 16+** with **pgvector** extension
   - Or use Docker (recommended for development)

6. **Docker & Docker Compose** (recommended)
   ```bash
   docker --version
   docker-compose --version
   ```

### API Keys

- **Claude API Key** - Required for LLM extraction and generation
  - Get from: https://console.anthropic.com/

## Quick Start with Docker (Recommended)

### 1. Clone and Configure

```bash
# Navigate to project directory
cd eligibility-gpt

# Copy environment template
copy .env.example .env

# Edit .env and add your Claude API key
# CLAUDE_API_KEY=your-actual-api-key-here
```

### 2. Start All Services

```bash
docker-compose up -d
```

This will start:
- PostgreSQL with pgvector (port 5432)
- Spring Boot Gateway (port 8080)
- Spring Boot Core Service (port 8081)
- FastAPI RAG Service (port 8000)
- React Frontend (port 3000)

### 3. Verify Services

```bash
# Check all containers are running
docker-compose ps

# Check gateway health
curl http://localhost:8080/actuator/health

# Check core service health
curl http://localhost:8081/actuator/health

# Check RAG service health
curl http://localhost:8000/health

# Access frontend
# Open browser: http://localhost:3000
```

### 4. View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f rag-service
docker-compose logs -f core-service
docker-compose logs -f gateway
```

### 5. Stop Services

```bash
docker-compose down

# Remove volumes (clears database)
docker-compose down -v
```

## Local Development Setup (Without Docker)

### 1. Database Setup

```bash
# Start PostgreSQL (if not already running)
# Create database
psql -U postgres
CREATE DATABASE eligibility_gpt;
CREATE USER eligibility_user WITH PASSWORD 'eligibility_pass';
GRANT ALL PRIVILEGES ON DATABASE eligibility_gpt TO eligibility_user;

# Install pgvector extension
# Follow: https://github.com/pgvector/pgvector#installation

# Connect to database
\c eligibility_gpt

# Enable extension
CREATE EXTENSION vector;

# Run init scripts
\i backend/db-init/01-init-schemas.sql
\i backend/db-init/02-create-core-tables.sql
\i backend/db-init/03-create-rag-tables.sql
```

### 2. RAG Service (FastAPI)

```bash
cd backend/rag-service

# Create virtual environment
python -m venv venv

# Activate virtual environment
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Download spaCy model
python -m spacy download en_core_web_sm

# Set environment variables
set DATABASE_URL=postgresql://eligibility_user:eligibility_pass@localhost:5432/eligibility_gpt
set CLAUDE_API_KEY=your-api-key-here

# Run service
python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 3. Core Service (Spring Boot)

```bash
cd backend/core-service

# Build
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Or run JAR directly
java -jar target/core-service-0.1.0.jar
```

### 4. Gateway (Spring Boot)

```bash
cd backend/gateway

# Build
mvn clean package -DskipTests

# Set environment variables
set CORE_SERVICE_URL=http://localhost:8081
set RAG_SERVICE_URL=http://localhost:8000

# Run
mvn spring-boot:run
```

### 5. Frontend (React + Vite)

```bash
cd frontend

# Install dependencies
npm install

# Set environment variable
echo VITE_API_BASE_URL=http://localhost:8080 > .env.local

# Run development server
npm run dev

# Build for production
npm run build
```

## Configuration

### Environment Variables

#### Core Service (`backend/core-service`)
```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/eligibility_gpt
SPRING_DATASOURCE_USERNAME=eligibility_user
SPRING_DATASOURCE_PASSWORD=eligibility_pass
JWT_SECRET=your-256-bit-secret-key-change-in-production
RAG_SERVICE_URL=http://localhost:8000
```

#### RAG Service (`backend/rag-service`)
```properties
DATABASE_URL=postgresql://eligibility_user:eligibility_pass@localhost:5432/eligibility_gpt
CLAUDE_API_KEY=your-claude-api-key
MODEL_CACHE_DIR=./models
UPLOAD_DIR=./uploads
```

#### Gateway (`backend/gateway`)
```properties
CORE_SERVICE_URL=http://localhost:8081
RAG_SERVICE_URL=http://localhost:8000
```

#### Frontend (`frontend`)
```properties
VITE_API_BASE_URL=http://localhost:8080
```

## Testing

### Backend Tests

```bash
# Core Service
cd backend/core-service
mvn test

# Gateway
cd backend/gateway
mvn test

# RAG Service
cd backend/rag-service
pytest
```

### Frontend Tests

```bash
cd frontend
npm test
```

## Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
pg_isready

# Check pgvector extension
psql -U eligibility_user -d eligibility_gpt -c "SELECT * FROM pg_extension WHERE extname = 'vector';"
```

### Port Conflicts

If ports are already in use, modify in `docker-compose.yml` or service configs:
- Gateway: 8080
- Core Service: 8081
- RAG Service: 8000
- Frontend: 3000
- PostgreSQL: 5432

### Model Download Issues

```bash
# RAG service downloads models on first run
# If slow/failing, pre-download:
cd backend/rag-service
python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('BAAI/bge-large-en-v1.5')"
```

### Memory Issues

RAG service needs ~4GB RAM for embedding models. If running all services locally:
- Minimum: 8GB RAM
- Recommended: 16GB RAM

## Next Steps

1. **Upload a test PDF** - Use the frontend or API
2. **Create a user profile** - Set category, DOB, education
3. **Ask eligibility questions** - Test the RAG pipeline
4. **Check batch eligibility** - Get all eligible posts

See [API Documentation](http://localhost:8000/docs) when RAG service is running.

## Development Workflow

### Adding New Features

1. **Database changes** - Update SQL in `backend/db-init/` and JPA entities
2. **Backend logic** - Add controllers, services, repositories
3. **RAG pipeline** - Modify `backend/rag-service/app/`
4. **API integration** - Update gateway routes if needed
5. **Frontend** - Add React components and API calls

### Code Style

- **Java**: Follow Spring Boot conventions, use Lombok
- **Python**: PEP 8, use Black formatter (`black .`)
- **React**: ESLint + Prettier

### Commit Messages

Follow conventional commits:
```
feat: add batch eligibility scan endpoint
fix: correct age calculation for leap years
docs: update setup guide
refactor: extract LLM prompt templates
```

## Production Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for production deployment guide.

## License

MIT License - see [LICENSE](LICENSE)
