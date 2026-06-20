import time
from fastapi import APIRouter
from app.config import settings

router = APIRouter()

START_TIME = time.time()


@router.get("/health")
def health():
    return {
        "status":  "ok",
        "model":   settings.model_name,
        "version": settings.version,
        "uptime_seconds": round(time.time() - START_TIME, 2),
    }


@router.get("/")
def root():
    return {
        "name":        "Mod-1.0",
        "description": "AI moderation API for bots — detect toxicity, hate, spam, and more.",
        "version":     settings.version,
        "docs":        "/docs",
        "endpoints": {
            "POST /v1/moderations": "OpenAI-compatible moderation check",
            "POST /v1/analyze":     "Detailed moderation analysis with recommendations",
            "POST /v1/classify":    "Classify text into specific categories",
            "GET  /v1/models":      "List available models",
            "POST /v1/keys":        "Create an API key (admin only)",
            "GET  /v1/keys":        "List all API keys (admin only)",
            "DELETE /v1/keys/{id}": "Revoke an API key (admin only)",
            "GET  /health":         "Health check",
        },
    }
