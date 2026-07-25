"""Schemas for PDF upload and ingestion"""

from pydantic import BaseModel
from typing import Optional
from datetime import date


class PdfMetadata(BaseModel):
    """Metadata for uploaded PDF"""
    title: str
    exam_body: Optional[str] = None
    notification_number: Optional[str] = None
    notification_date: Optional[date] = None
    application_start_date: Optional[date] = None
    application_end_date: Optional[date] = None


class IngestionStatusResponse(BaseModel):
    """Status of PDF ingestion"""
    notification_id: int
    status: str  # processing, ready, failed
    progress: Optional[dict] = None
    total_pages: Optional[int] = None
    total_posts: Optional[int] = None
    error: Optional[str] = None
