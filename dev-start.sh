#!/bin/bash

# ElgibilityGPT Development Startup Script
# This script helps you start all services for local development

set -e  # Exit on error

echo "======================================"
echo "  ElgibilityGPT Development Startup"
echo "======================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found${NC}"
    echo "Copying .env.example to .env..."
    cp .env.example .env
    echo -e "${RED}IMPORTANT: Edit .env and add your CLAUDE_API_KEY before continuing!${NC}"
    exit 1
fi

# Check if Claude API key is set
if ! grep -q "CLAUDE_API_KEY=sk-" .env; then
    echo -e "${RED}ERROR: CLAUDE_API_KEY not set in .env file${NC}"
    echo "Please edit .env and add your Claude API key"
    exit 1
fi

echo -e "${GREEN}Environment file found and configured${NC}"
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}ERROR: Docker is not running${NC}"
    echo "Please start Docker Desktop and try again"
    exit 1
fi

echo -e "${GREEN}Docker is running${NC}"
echo ""

# Ask user for startup mode
echo "Choose startup mode:"
echo "1) Full Docker Compose (recommended for quick start)"
echo "2) Services only (DB + Gateway + Core + RAG via Docker, manual Frontend)"
echo "3) Database only (run all services manually)"
echo ""
read -p "Enter choice [1-3]: " choice

case $choice in
    1)
        echo ""
        echo "Starting all services with Docker Compose..."
        echo ""
        docker-compose up -d
        echo ""
        echo -e "${GREEN}All services started!${NC}"
        echo ""
        echo "Service URLs:"
        echo "  - Frontend:     http://localhost:3000"
        echo "  - Gateway:      http://localhost:8080"
        echo "  - Core Service: http://localhost:8081"
        echo "  - RAG Service:  http://localhost:8000"
        echo "  - RAG API Docs: http://localhost:8000/docs"
        echo "  - PostgreSQL:   localhost:5432"
        echo ""
        echo "View logs:"
        echo "  docker-compose logs -f"
        echo ""
        echo "Stop services:"
        echo "  docker-compose down"
        ;;
    
    2)
        echo ""
        echo "Starting backend services only..."
        echo ""
        docker-compose up -d postgres gateway core-service rag-service
        echo ""
        echo -e "${GREEN}Backend services started!${NC}"
        echo ""
        echo "To start frontend manually:"
        echo "  cd frontend"
        echo "  npm install"
        echo "  npm run dev"
        echo ""
        echo "Service URLs:"
        echo "  - Gateway:      http://localhost:8080"
        echo "  - Core Service: http://localhost:8081"
        echo "  - RAG Service:  http://localhost:8000"
        echo "  - RAG API Docs: http://localhost:8000/docs"
        echo "  - PostgreSQL:   localhost:5432"
        ;;
    
    3)
        echo ""
        echo "Starting PostgreSQL only..."
        echo ""
        docker-compose up -d postgres
        echo ""
        echo -e "${GREEN}PostgreSQL started!${NC}"
        echo ""
        echo "Database connection:"
        echo "  Host: localhost"
        echo "  Port: 5432"
        echo "  Database: eligibility_gpt"
        echo "  User: eligibility_user"
        echo "  Password: eligibility_pass"
        echo ""
        echo "To start services manually:"
        echo ""
        echo "1. RAG Service:"
        echo "   cd backend/rag-service"
        echo "   python -m venv venv"
        echo "   source venv/bin/activate  # or venv\\Scripts\\activate on Windows"
        echo "   pip install -r requirements.txt"
        echo "   uvicorn app.main:app --reload"
        echo ""
        echo "2. Core Service:"
        echo "   cd backend/core-service"
        echo "   mvn spring-boot:run"
        echo ""
        echo "3. Gateway:"
        echo "   cd backend/gateway"
        echo "   mvn spring-boot:run"
        echo ""
        echo "4. Frontend:"
        echo "   cd frontend"
        echo "   npm install"
        echo "   npm run dev"
        ;;
    
    *)
        echo -e "${RED}Invalid choice${NC}"
        exit 1
        ;;
esac

echo ""
echo -e "${GREEN}Setup complete!${NC}"
echo ""
echo "Next steps:"
echo "1. Wait for all services to be healthy (30-60 seconds)"
echo "2. Check service health:"
echo "   curl http://localhost:8080/actuator/health"
echo "   curl http://localhost:8000/health"
echo "3. See API documentation: http://localhost:8000/docs"
echo "4. Read SETUP.md for detailed instructions"
echo ""
