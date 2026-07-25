"""
FastAPI RAG/ML Service for ElgibilityGPT
Handles PDF ingestion, embeddings, LLM extraction, and eligibility Q&A
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging

from app.config import settings
from app.api import health, query, eligibility
from app.db.database import engine, init_db
# from app.services.embedding_service import EmbeddingService  # TODO: implement

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Lifecycle manager for startup and shutdown events"""
    # Startup
    logger.info("Starting ElgibilityGPT RAG Service...")
    
    # Initialize database
    await init_db()
    logger.info("Database initialized")
    
    # TODO: Initialize embedding model when implemented
    # embedding_service = EmbeddingService()
    # await embedding_service.initialize()
    # logger.info("Embedding service initialized")
    
    yield
    
    # Shutdown
    logger.info("Shutting down RAG service...")
    await engine.dispose()


# Create FastAPI app
app = FastAPI(
    title="ElgibilityGPT RAG Service",
    description="RAG/ML service for government exam eligibility checking",
    version="0.1.0",
    lifespan=lifespan
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health.router, tags=["health"])
app.include_router(query.router, tags=["query"])
app.include_router(eligibility.router, tags=["eligibility"])


@app.get("/")
async def root():
    return {
        "service": "ElgibilityGPT RAG Service",
        "version": "0.1.0",
        "status": "running",
        "docs": "/docs"
    }


@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "service": "rag-service",
        "version": "0.1.0"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
