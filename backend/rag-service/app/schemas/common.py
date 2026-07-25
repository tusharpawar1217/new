"""Common schemas used across the application"""

from pydantic import BaseModel, Field
from typing import Optional, Dict, Any
from datetime import date
from enum import Enum


class Gender(str, Enum):
    MALE = "MALE"
    FEMALE = "FEMALE"
    OTHER = "OTHER"


class Category(str, Enum):
    GENERAL = "GENERAL"
    OBC = "OBC"
    SC = "SC"
    ST = "ST"
    EWS = "EWS"


class UserProfile(BaseModel):
    """User profile for eligibility checking"""
    date_of_birth: Optional[date] = None
    gender: Optional[Gender] = None
    category: Optional[Category] = None
    is_pwbd: Optional[bool] = False
    pwbd_type: Optional[str] = None
    is_ex_serviceman: Optional[bool] = False
    domicile_state: Optional[str] = None
    education_level: Optional[str] = None
    education_specialization: Optional[str] = None
    extra_attributes: Dict[str, Any] = Field(default_factory=dict)
    
    class Config:
        use_enum_values = True


class PostSummary(BaseModel):
    """Brief summary of a post"""
    id: int
    post_name: str
    post_code: Optional[str] = None
    

class Citation(BaseModel):
    """Source citation for an answer"""
    page: int
    section: Optional[str] = None
    snippet: Optional[str] = None
