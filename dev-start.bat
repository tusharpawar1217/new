@echo off
REM ElgibilityGPT Development Startup Script for Windows

echo ======================================
echo   ElgibilityGPT Development Startup
echo ======================================
echo.

REM Check if .env file exists
if not exist .env (
    echo Warning: .env file not found
    echo Copying .env.example to .env...
    copy .env.example .env
    echo IMPORTANT: Edit .env and add your CLAUDE_API_KEY before continuing!
    pause
    exit /b 1
)

REM Check if Claude API key is set
findstr /C:"CLAUDE_API_KEY=sk-" .env >nul
if errorlevel 1 (
    echo ERROR: CLAUDE_API_KEY not set in .env file
    echo Please edit .env and add your Claude API key
    pause
    exit /b 1
)

echo Environment file found and configured
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running
    echo Please start Docker Desktop and try again
    pause
    exit /b 1
)

echo Docker is running
echo.

REM Ask user for startup mode
echo Choose startup mode:
echo 1) Full Docker Compose (recommended for quick start)
echo 2) Services only (DB + Gateway + Core + RAG via Docker, manual Frontend)
echo 3) Database only (run all services manually)
echo.
set /p choice="Enter choice [1-3]: "

if "%choice%"=="1" goto full_docker
if "%choice%"=="2" goto services_only
if "%choice%"=="3" goto db_only
goto invalid_choice

:full_docker
echo.
echo Starting all services with Docker Compose...
echo.
docker-compose up -d
echo.
echo All services started!
echo.
echo Service URLs:
echo   - Frontend:     http://localhost:3000
echo   - Gateway:      http://localhost:8080
echo   - Core Service: http://localhost:8081
echo   - RAG Service:  http://localhost:8000
echo   - RAG API Docs: http://localhost:8000/docs
echo   - PostgreSQL:   localhost:5432
echo.
echo View logs:
echo   docker-compose logs -f
echo.
echo Stop services:
echo   docker-compose down
goto end

:services_only
echo.
echo Starting backend services only...
echo.
docker-compose up -d postgres gateway core-service rag-service
echo.
echo Backend services started!
echo.
echo To start frontend manually:
echo   cd frontend
echo   npm install
echo   npm run dev
echo.
echo Service URLs:
echo   - Gateway:      http://localhost:8080
echo   - Core Service: http://localhost:8081
echo   - RAG Service:  http://localhost:8000
echo   - RAG API Docs: http://localhost:8000/docs
echo   - PostgreSQL:   localhost:5432
goto end

:db_only
echo.
echo Starting PostgreSQL only...
echo.
docker-compose up -d postgres
echo.
echo PostgreSQL started!
echo.
echo Database connection:
echo   Host: localhost
echo   Port: 5432
echo   Database: eligibility_gpt
echo   User: eligibility_user
echo   Password: eligibility_pass
echo.
echo To start services manually:
echo.
echo 1. RAG Service:
echo    cd backend\rag-service
echo    python -m venv venv
echo    venv\Scripts\activate
echo    pip install -r requirements.txt
echo    uvicorn app.main:app --reload
echo.
echo 2. Core Service:
echo    cd backend\core-service
echo    mvn spring-boot:run
echo.
echo 3. Gateway:
echo    cd backend\gateway
echo    mvn spring-boot:run
echo.
echo 4. Frontend:
echo    cd frontend
echo    npm install
echo    npm run dev
goto end

:invalid_choice
echo ERROR: Invalid choice
pause
exit /b 1

:end
echo.
echo Setup complete!
echo.
echo Next steps:
echo 1. Wait for all services to be healthy (30-60 seconds)
echo 2. Check service health:
echo    curl http://localhost:8080/actuator/health
echo    curl http://localhost:8000/health
echo 3. See API documentation: http://localhost:8000/docs
echo 4. Read SETUP.md for detailed instructions
echo.
pause
