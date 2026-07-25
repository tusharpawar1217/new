"""SQLAlchemy ORM models for RAG schema"""

from sqlalchemy import Column, Integer, String, Text, Float, ARRAY, TIMESTAMP, ForeignKey, CheckConstraint
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.sql import func
from pgvector.sqlalchemy import Vector
from app.db.database import Base


class DocumentChunk(Base):
    """Document chunks with vector embeddings"""
    __tablename__ = 'document_chunks'
    __table_args__ = {'schema': 'rag'}

    id = Column(Integer, primary_key=True, index=True)
    notification_id = Column(Integer, nullable=False, index=True)
    page_number = Column(Integer, nullable=False, index=True)
    section_title = Column(String(255))
    chunk_type = Column(String(50), index=True)  # eligibility, fee, dates, exam_pattern, general, annexure
    content = Column(Text, nullable=False)
    token_count = Column(Integer)
    chunk_index = Column(Integer)  # order within notification
    
    # Vector embedding - adjust dimension based on model
    embedding = Column(Vector(768))  # BGE-large-en-v1.5 = 1024, adjust if different
    
    # Dynamic metadata as JSONB
    metadata = Column(JSONB, default={})
    
    created_at = Column(TIMESTAMP, server_default=func.now())


class Post(Base):
    """Posts with dynamic JSONB eligibility schema"""
    __tablename__ = 'posts'
    __table_args__ = {'schema': 'rag'}

    id = Column(Integer, primary_key=True, index=True)
    notification_id = Column(Integer, nullable=False, index=True)
    post_name = Column(String(500), nullable=False)
    post_code = Column(String(100), index=True)
    
    # THE CRITICAL DYNAMIC SCHEMA - stores full eligibility as JSONB
    # Shape varies per exam - no migration needed for new exam types
    eligibility_schema = Column(JSONB, nullable=False)
    
    # Source tracking
    source_pages = Column(ARRAY(Integer))
    extraction_confidence = Column(Float, CheckConstraint('extraction_confidence >= 0 AND extraction_confidence <= 1'))
    
    created_at = Column(TIMESTAMP, server_default=func.now())
    updated_at = Column(TIMESTAMP, server_default=func.now(), onupdate=func.now())


class EligibilityRule(Base):
    """Decomposed rules for evaluator dispatch"""
    __tablename__ = 'eligibility_rules'
    __table_args__ = {'schema': 'rag'}

    id = Column(Integer, primary_key=True, index=True)
    post_id = Column(Integer, ForeignKey('rag.posts.id', ondelete='CASCADE'), nullable=False, index=True)
    
    # Rule categorization for evaluator dispatch
    rule_key = Column(String(100), nullable=False, index=True)
    rule_type = Column(String(50), nullable=False, index=True)  # age, education, category, gender, domicile, custom
    
    # Dynamic rule definition as JSONB
    rule_definition = Column(JSONB, nullable=False)
    
    source_page = Column(Integer)
    created_at = Column(TIMESTAMP, server_default=func.now())


class ProcessingLog(Base):
    """Audit trail for PDF ingestion pipeline"""
    __tablename__ = 'processing_log'
    __table_args__ = {'schema': 'rag'}

    id = Column(Integer, primary_key=True, index=True)
    notification_id = Column(Integer, nullable=False, index=True)
    stage = Column(String(100), nullable=False, index=True)  # parsing, chunking, embedding, extraction, completed, failed
    status = Column(String(50), nullable=False, index=True)  # in_progress, completed, failed
    message = Column(Text)
    details = Column(JSONB)
    started_at = Column(TIMESTAMP, server_default=func.now())
    completed_at = Column(TIMESTAMP)


class EligibilityCache(Base):
    """Performance optimization for eligibility checks"""
    __tablename__ = 'eligibility_cache'
    __table_args__ = {'schema': 'rag'}

    id = Column(Integer, primary_key=True, index=True)
    post_id = Column(Integer, ForeignKey('rag.posts.id', ondelete='CASCADE'), nullable=False, index=True)
    profile_hash = Column(String(64), nullable=False, index=True)  # SHA256 of normalized profile
    is_eligible = Column(Integer, nullable=False)  # SQLite doesn't have boolean, use 0/1
    verdict_text = Column(Text, nullable=False)
    missing_fields = Column(JSONB, default=[])
    reasoning = Column(JSONB)  # detailed per-rule check results
    checked_at = Column(TIMESTAMP, server_default=func.now(), index=True)
