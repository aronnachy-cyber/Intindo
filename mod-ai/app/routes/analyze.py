from fastapi import APIRouter, Depends
from pydantic import BaseModel
from app.auth import get_api_key
from app.moderator import build_analysis_result

router = APIRouter()


class AnalyzeInput(BaseModel):
    input: str
    model: str = "mod-1.0"


@router.post("/v1/analyze")
def analyze(body: AnalyzeInput, _key: str = Depends(get_api_key)):
    return build_analysis_result(body.input)
