"""Schemas for Q&A query endpoints"""

from pydantic import BaseModel, Field
from typing import List, Optional
from app.schemas.common import Citation


class QueryRequest(BaseModel):
    """Request for Q&A query"""
    notification_id: int
    question: str = Field(..., min_length=3, max_length=500)
    session_id: Optional[str] = None


class QueryResponse(BaseModel):
    """Response for Q&A query"""
    answer: str
    sources: List[Citation]
    confidence: float = Field(..., ge=0.0, le=1.0)
    answer_type: str  # GROUNDED, NOT_FOUND, INSUFFICIENT_CONTEXT
