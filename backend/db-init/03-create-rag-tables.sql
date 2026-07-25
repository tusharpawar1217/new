-- RAG service tables (managed by FastAPI/Python)
-- CRITICAL: Dynamic JSONB design for exam-agnostic eligibility rules

-- Document chunks table (vector embeddings + metadata)
CREATE TABLE IF NOT EXISTS rag.document_chunks (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL, -- References core.notifications(id)
    page_number INTEGER NOT NULL,
    section_title VARCHAR(255),
    chunk_type VARCHAR(50), -- eligibility, fee, dates, exam_pattern, general, annexure
    content TEXT NOT NULL,
    token_count INTEGER,
    chunk_index INTEGER, -- order within notification
    
    -- Vector embedding
    embedding VECTOR(768), -- BGE-large-en-v1.5 = 1024 dim, adjust if using different model
    
    -- DYNAMIC: Arbitrary per-chunk metadata (post_code, table_ref, etc.)
    metadata JSONB DEFAULT '{}'::JSONB,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Posts table - THE CORE DYNAMIC TABLE
-- Each post's eligibility rules stored as JSONB - shape varies per exam
CREATE TABLE IF NOT EXISTS rag.posts (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL, -- References core.notifications(id)
    post_name VARCHAR(500) NOT NULL,
    post_code VARCHAR(100), -- may be null if PDF doesn't assign codes
    
    -- DYNAMIC ELIGIBILITY SCHEMA (THE KEY DESIGN DECISION)
    -- This JSONB field stores the FULL extracted eligibility object
    -- Shape varies per exam body - onboard new exams without migration
    -- Example structure:
    -- {
    --   "age": {"min": 18, "max": 30, "as_on_date": "2026-01-01",
    --           "relaxations": {"OBC": 3, "SC": 5, "ST": 5, "PwBD": 10}},
    --   "education": ["Bachelor's Degree in any discipline",
    --                 "or equivalent qualification"],
    --   "category_eligibility": {"GENERAL": true, "OBC": true, ...},
    --   "gender": null,  -- null = any gender
    --   "domicile": null,  -- null = all India
    --   "custom_fields": {
    --     "departmental_quota": false,
    --     "defence_background_preferred": true,
    --     "sportsperson_quota_available": true
    --   }
    -- }
    eligibility_schema JSONB NOT NULL,
    
    -- Source tracking
    source_pages INTEGER[], -- pages where this post's criteria were found
    extraction_confidence FLOAT CHECK (extraction_confidence >= 0 AND extraction_confidence <= 1),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Eligibility rules table - Decomposed rule-by-rule for the rule engine
-- Each row = one extractedule, keyed by rule_type for evaluator dispatch
CREATE TABLE IF NOT EXISTS rag.eligibility_rules (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT REFERENCES rag.posts(id) ON DELETE CASCADE,
    
    -- Rule categorization for evaluator dispatch
    rule_key VARCHAR(100) NOT NULL, -- e.g. "age", "education_degree", "category_obc_relaxation"
    rule_type VARCHAR(50) NOT NULL, -- age, education, category, gender, domicile, custom
    
    -- DYNAMIC: The actual rule definition as JSONB
    -- Shape varies by rule_type:
    -- age: {"min": 18, "max": 30, "as_on_date": "2026-01-01", "relaxation": 3}
    -- education: {"level": "Bachelor's", "specialization": null, "or_equivalent": true}
    -- category: {"category": "OBC", "relaxation_years": 3}
    -- custom: {"key": "departmental_quota", "required": true, "value": "yes"}
    rule_definition JSONB NOT NULL,
    
    source_page INTEGER, -- page where this specific rule was found
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Document processing log (audit trail for ingestion pipeline)
CREATE TABLE IF NOT EXISTS rag.processing_log (
    id BIGSERIAL PRIMARY KEY,
    notification_id BIGINT NOT NULL,
    stage VARCHAR(100) NOT NULL, -- parsing, chunking, embedding, extraction, completed, failed
    status VARCHAR(50) NOT NULL, -- in_progress, completed, failed
    message TEXT,
    details JSONB,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Eligibility check cache (performance optimization)
CREATE TABLE IF NOT EXISTS rag.eligibility_cache (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT REFERENCES rag.posts(id) ON DELETE CASCADE,
    profile_hash VARCHAR(64) NOT NULL, -- SHA256 hash of normalized profile JSON
    is_eligible BOOLEAN NOT NULL,
    verdict_text TEXT NOT NULL,
    missing_fields JSONB DEFAULT '[]'::JSONB,
    reasoning JSONB, -- detailed per-rule check results
    checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, profile_hash)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_chunks_notification_id ON rag.document_chunks(notification_id);
CREATE INDEX IF NOT EXISTS idx_chunks_chunk_type ON rag.document_chunks(chunk_type);
CREATE INDEX IF NOT EXISTS idx_chunks_page_number ON rag.document_chunks(page_number);

-- Vector similarity index (HNSW for fast approximate nearest neighbor search)
-- This is CRITICAL for performance - without it, vector search is O(n)
CREATE INDEX IF NOT EXISTS idx_chunks_embedding_hnsw ON rag.document_chunks 
USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

-- GIN indexes on JSONB columns for fast queries
CREATE INDEX IF NOT EXISTS idx_chunks_metadata_gin ON rag.document_chunks USING gin(metadata);

CREATE INDEX IF NOT EXISTS idx_posts_notification_id ON rag.posts(notification_id);
CREATE INDEX IF NOT EXISTS idx_posts_post_code ON rag.posts(post_code);
-- GIN index on eligibility_schema for querying nested JSONB
CREATE INDEX IF NOT EXISTS idx_posts_eligibility_schema_gin ON rag.posts USING gin(eligibility_schema jsonb_path_ops);

CREATE INDEX IF NOT EXISTS idx_rules_post_id ON rag.eligibility_rules(post_id);
CREATE INDEX IF NOT EXISTS idx_rules_rule_type ON rag.eligibility_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_rules_rule_key ON rag.eligibility_rules(rule_key);
-- GIN index on rule_definition for complex queries
CREATE INDEX IF NOT EXISTS idx_rules_definition_gin ON rag.eligibility_rules USING gin(rule_definition);

CREATE INDEX IF NOT EXISTS idx_processing_log_notification_id ON rag.processing_log(notification_id);
CREATE INDEX IF NOT EXISTS idx_processing_log_stage ON rag.processing_log(stage);
CREATE INDEX IF NOT EXISTS idx_processing_log_status ON rag.processing_log(status);

CREATE INDEX IF NOT EXISTS idx_cache_post_id ON rag.eligibility_cache(post_id);
CREATE INDEX IF NOT EXISTS idx_cache_profile_hash ON rag.eligibility_cache(profile_hash);
CREATE INDEX IF NOT EXISTS idx_cache_checked_at ON rag.eligibility_cache(checked_at);
