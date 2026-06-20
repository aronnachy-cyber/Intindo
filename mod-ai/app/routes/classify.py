from fastapi import APIRouter, Depends
from pydantic import BaseModel
from typing import List, Optional
from app.auth import get_api_key
from app.moderator import build_classify_result

router = APIRouter()


class ClassifyInput(BaseModel):
    input: str
    categories: Optional[List[str]] = None
    model: str = "mod-1.0"


VALID_CATEGORIES = [
    "toxicity", "hate", "harassment",
    "self-harm", "sexual", "violence",
    "spam", "profanity"
]


@router.post("/v1/classify")
def classify(body: ClassifyInput, _key: str = Depends(get_api_key)):
    cats = body.categories
    if cats:
        invalid = [c for c in cats if c not in VALID_CATEGORIES]
        if invalid:
            return {
                "error": f"Unknown categories: {invalid}. Valid: {VALID_CATEGORIES}"
            }
    return build_classify_result(body.input, cats)
