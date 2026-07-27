-- Core service tables (Spring Boot JPA) - PRODUCTION SCHEMA with Dynamic JSONB Design
-- Every exam has different fields - JSONB allows onboarding new exams without migrations

-- Users table
CREATE TABLE IF NOT EXISTS core.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'USER', -- USER, ADMIN
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- User profiles - Fixed columns for universal fields + JSONB for exam-specific attributes
CREATE TABLE IF NOT EXISTS core.user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES core.users(id) ON DELETE CASCADE,
    
    -- Universal fixed columns (exist across ALL government exams)
    date_of_birth DATE,
    gender VARCHAR(20), -- MALE, FEMALE, OTHER
    category VARCHAR(50), -- GENERAL, OBC, SC, ST, EWS
    is_pwbd BOOLEAN DEFAULT FALSE,
    pwbd_type VARCHAR(100),
    is_ex_serviceman BOOLEAN DEFAULT FALSE,
    domicile_state VARCHAR(100),
    education_level VARCHAR(100), -- Bachelor's, Master's, Doctorate, etc.
    education_specialization VARCHAR(255),
    
    -- DYNAMIC: Exam-specific fields that don't fit fixed columns
    -- Examples: departmental_quota, sportsperson_category, defence_background, etc.
    extra_attributes JSONB DEFAULT '{}'::JSONB,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications (uploaded PDFs) - renamed from job_postings for clarity
CREATE TABLE IF NOT EXISTS core.notifications (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    exam_body VARCHAR(255), -- SSC, RRB, IBPS, UPSC, State PSC, etc.
    notification_number VARCHAR(100),
    source_filename VARCHAR(500), -- original uploaded filename
    pdf_file_path VARCHAR(500), -- storage path
    pdf_file_size_bytes BIGINT,
    total_pages INTEGER,
    
    -- Processing status workflow
    status VARCHAR(50) DEFAULT 'processing', -- processing, ready, failed
    processing_started_at TIMESTAMP,
    processing_completed_at TIMESTAMP,
    processing_error TEXT,
    
    -- Dates from the notification
    notification_date DATE,
    application_start_date DATE,
    application_end_date DATE,
    exam_date DATE,
    
    uploaded_by BIGINT REFERENCES core.users(id),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Session profiles (temporary, for anonymous/testing users)
CREATE TABLE IF NOT EXISTS core.session_profiles (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL,
    user_id BIGINT REFERENCES core.users(id) ON DELETE CASCADE, -- optional, null for anonymous
    profile_data JSONB NOT NULL, -- full profile as JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '24 hours'),
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON core.users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON core.users(role);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON core.user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_category ON core.user_profiles(category);

CREATE INDEX IF NOT EXISTS idx_notifications_exam_body ON core.notifications(exam_body);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON core.notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_uploaded_by ON core.notifications(uploaded_by);

CREATE INDEX IF NOT EXISTS idx_session_profiles_session_id ON core.session_profiles(session_id);
CREATE INDEX IF NOT EXISTS idx_session_profiles_expires_at ON core.session_profiles(expires_at);

-- GIN indexes on JSONB columns for fast queries
CREATE INDEX IF NOT EXISTS idx_user_profiles_extra_attrs_gin ON core.user_profiles USING gin(extra_attributes);
CREATE INDEX IF NOT EXISTS idx_session_profiles_data_gin ON core.session_profiles USING gin(profile_data);
