"""Q&A query endpoints"""

from fastapi import APIRouter, HTTPException
from app.schemas.query import QueryRequest, QueryResponse
from app.schemas.common import Citation
import logging

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post("/api/rag/query", response_model=QueryResponse)
async def query_notification(request: QueryRequest):
    """
    Q&A query endpoint - grounded RAG with citations
    
    This is a stub implementation. Full implementation requires:
    - Retrieval service (hybrid search)
    - LLM service (Claude API)
    - Context formatting and prompt engineering
    """
    logger.info(f"Query request for notification {request.notification_id}: {request.question}")
    
    # TODO: Implement full RAG pipeline
    # For now, return a stub response
    return QueryResponse(
        answer="This is a stub response. The full RAG pipeline (retrieval + LLM generation) needs to be implemented. "
               "See backend/rag-service/app/services/ for service implementations.",
        sources=[
            Citation(page=1, section="Introduction", snippet="Sample citation")
        ],
        confidence=0.0,
        answer_type="NOT_FOUND"
    )
