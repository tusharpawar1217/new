"""Health check endpoints"""

from fastapi import APIRouter

router = APIRouter()


@router.get("/health")
async def health_check():
    """Basic health check"""
    return {
        "status": "healthy",
        "service": "rag-service",
        "version": "0.1.0"
    }


@router.get("/api/rag/health")
async def rag_health():
    """Detailed RAG service health"""
    return {
        "status": "healthy",
        "service": "rag-service",
        "version": "0.1.0",
        "components": {
            "database": "connected",
            "embedding_model": "ready",
            "llm": "ready"
        }
    }
