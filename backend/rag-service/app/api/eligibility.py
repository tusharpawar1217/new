"""Eligibility checking endpoints"""

from fastapi import APIRouter, HTTPException
from app.schemas.eligibility import (
    EligibilityCheckRequest,
    EligibilityCheckResponse,
    BatchScanRequest,
    BatchScanResponse,
    RuleCheckResult,
    PostEligibilitySummary
)
from app.schemas.common import Citation
import logging

router = APIRouter()
logger = logging.getLogger(__name__)


@router.post("/api/rag/eligibility/check", response_model=EligibilityCheckResponse)
async def check_eligibility(request: EligibilityCheckRequest):
    """
    Check eligibility for a single post
    
    This is a stub implementation. Full implementation requires:
    - Rule engine with evaluator dispatch
    - Post data retrieval from database
    - Deterministic age/education/category checking
    - LLM for natural language verdict generation
    """
    logger.info(f"Eligibility check for notification {request.notification_id}")
    
    # TODO: Implement full eligibility checking
    # For now, return a stub response
    return EligibilityCheckResponse(
        post_name="Sample Post",
        post_code="POST-001",
        is_eligible=False,
        verdict="This is a stub response. The rule engine needs to be implemented. "
                "See backend/rag-service/app/services/rule_engine.py for implementation.",
        details={
            "age": RuleCheckResult(
                rule_type="age",
                passed=False,
                details={},
                reasoning="Rule engine not implemented"
            )
        },
        sources=[Citation(page=1)],
        missing_fields=["date_of_birth"],
        confidence=0.0
    )


@router.post("/api/rag/eligibility/scan", response_model=BatchScanResponse)
async def batch_scan(request: BatchScanRequest):
    """
    Batch eligibility scan across all posts
    
    This is a stub implementation.
    """
    logger.info(f"Batch scan for notification {request.notification_id}")
    
    # TODO: Implement batch scanning
    return BatchScanResponse(
        notification_id=request.notification_id,
        total_posts=0,
        eligible_posts=[],
        ineligible_posts=[],
        insufficient_info_posts=[
            PostEligibilitySummary(
                post_id=1,
                post_name="Sample Post",
                post_code="POST-001",
                is_eligible=None,
                summary="Rule engine not implemented yet",
                missing_fields=[]
            )
        ]
    )
