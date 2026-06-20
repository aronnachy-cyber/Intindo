import time
from fastapi import APIRouter, Depends
from app.auth import get_api_key

router = APIRouter()

MODELS = [
    {
        "id":         "mod-1.0",
        "object":     "model",
        "created":    1750000000,
        "owned_by":   "mod-ai",
        "capabilities": [
            "moderations",
            "analyze",
            "classify",
        ],
        "categories": [
            "toxicity",
            "hate",
            "harassment",
            "self-harm",
            "sexual",
            "violence",
            "spam",
            "profanity",
        ],
        "description": (
            "Mod-1.0 is a moderation AI designed to protect bot-powered communities. "
            "It detects toxicity, hate speech, harassment, spam, and more."
        ),
    }
]


@router.get("/v1/models")
def list_models(_key: str = Depends(get_api_key)):
    return {
        "object": "list",
        "data":   MODELS,
    }


@router.get("/v1/models/{model_id}")
def get_model(model_id: str, _key: str = Depends(get_api_key)):
    for m in MODELS:
        if m["id"] == model_id:
            return m
    return {"error": f"Model '{model_id}' not found"}, 404
