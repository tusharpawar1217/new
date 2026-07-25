"""Schemas for eligibility checking endpoints"""

from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from app.schemas.common import UserProfile, Citation


class EligibilityCheckRequest(BaseModel):
    """Request for single post eligibility check"""
    notification_id: int
    post_id: Optional[int] = None
    post_code: Optional[str] = None
    post_name: Optional[str] = None
    user_profile: UserProfile


class RuleCheckResult(BaseModel):
    """Result of a single rule check"""
    rule_type: str
    passed: bool
    details: Dict[str, Any]
    reasoning: str


class EligibilityCheckResponse(BaseModel):
    """Response for eligibility check"""
    post_name: str
    post_code: Optional[str] = None
    is_eligible: bool
    verdict: str  # Natural language verdict
    details: Dict[str, RuleCheckResult]
    sources: List[Citation]
    missing_fields: List[str]
    confidence: float = Field(..., ge=0.0, le=1.0)


class PostEligibilitySummary(BaseModel):
    """Summary of eligibility for one post"""
    post_id: int
    post_name: str
    post_code: Optional[str] = None
    is_eligible: Optional[bool] = None
    summary: str
    missing_fields: List[str] = Field(default_factory=list)


class BatchScanRequest(BaseModel):
    """Request for batch eligibility scan"""
    notification_id: int
    user_profile: UserProfile


class BatchScanResponse(BaseModel):
    """Response for batch eligibility scan"""
    notification_id: int
    total_posts: int
    eligible_posts: List[PostEligibilitySummary]
    ineligible_posts: List[PostEligibilitySummary]
    insufficient_info_posts: List[PostEligibilitySummary]
