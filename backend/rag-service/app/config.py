"""Configuration management for RAG service"""

from pydantic_settings import BaseSettings
from typing import List
import os


class Settings(BaseSettings):
    # Service
    SERVICE_NAME: str = "eligibility-gpt-rag-service"
    VERSION: str = "0.1.0"
    
    # Database
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL",
        "postgresql://eligibility_user:eligibility_pass@localhost:5432/eligibility_gpt"
    )
    
    # Claude API
    CLAUDE_API_KEY: str = os.getenv("CLAUDE_API_KEY", "")
    CLAUDE_MODEL: str = "claude-sonnet-4-5"
    CLAUDE_MAX_TOKENS: int = 4096
    CLAUDE_TEMPERATURE: float = 0.0
    
    # Embedding model
    EMBEDDING_MODEL: str = "BAAI/bge-large-en-v1.5"  # 1024 dimensions
    EMBEDDING_DIMENSION: int = 1024
    EMBEDDING_BATCH_SIZE: int = 32
    
    # Reranking model
    RERANKER_MODEL: str = "BAAI/bge-reranker-base"
    USE_RERANKER: bool = True
    
    # PDF processing
    MAX_PDF_SIZE_MB: int = 50
    UPLOAD_DIR: str = os.getenv("UPLOAD_DIR", "./uploads")
    TEMP_DIR: str = os.getenv("TEMP_DIR", "./temp")
    
    # Chunking
    CHUNK_SIZE: int = 512  # tokens
    CHUNK_OVERLAP: int = 50
    MIN_CHUNK_SIZE: int = 100
    
    # Retrieval
    TOP_K_RETRIEVAL: int = 20
    TOP_K_RERANK: int = 5
    MIN_SIMILARITY_SCORE: float = 0.5
    
    # BM25 settings
    BM25_K1: float = 1.5
    BM25_B: float = 0.75
    
    # Hybrid search weights
    DENSE_WEIGHT: float = 0.7
    SPARSE_WEIGHT: float = 0.3
    
    # Cache
    MODEL_CACHE_DIR: str = os.getenv("MODEL_CACHE_DIR", "./models")
    
    # CORS
    CORS_ORIGINS: List[str] = [
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:8080"
    ]
    
    # Logging
    LOG_LEVEL: str = "INFO"
    
    # Processing timeouts
    PDF_PARSE_TIMEOUT: int = 300  # 5 minutes
    EMBEDDING_TIMEOUT: int = 600  # 10 minutes
    EXTRACTION_TIMEOUT: int = 900  # 15 minutes
    
    class Config:
        env_file = ".env"
        case_sensitive = True


# Global settings instance
settings = Settings()

# Create directories if they don't exist
os.makedirs(settings.UPLOAD_DIR, exist_ok=True)
os.makedirs(settings.TEMP_DIR, exist_ok=True)
os.makedirs(settings.MODEL_CACHE_DIR, exist_ok=True)
