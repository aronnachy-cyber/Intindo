from fastapi import APIRouter, Depends
from pydantic import BaseModel
from typing import List, Optional
from app.auth import get_api_key
from app.moderator import build_moderation_result

router = APIRouter()


class ModerationInput(BaseModel):
    input: str | List[str]
    model: Optional[str] = "mod-1.0"


@router.post("/v1/moderations")
def create_moderation(body: ModerationInput, _key: str = Depends(get_api_key)):
    texts = body.input if isinstance(body.input, list) else [body.input]
    results = []
    for text in texts:
        r = build_moderation_result(text)
        results.append(r["results"][0])

    return {
        "id":      f"modr-batch-{len(texts)}",
        "model":   "mod-1.0",
        "results": results,
    }
