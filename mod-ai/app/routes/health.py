import time
from pathlib import Path
from fastapi import APIRouter, Request
from fastapi.responses import HTMLResponse
from app.config import settings

router = APIRouter()

START_TIME = time.time()

_html_template = (Path(__file__).parent.parent / "static" / "index.html").read_text()


@router.get("/", response_class=HTMLResponse)
def root(request: Request):
    base_url = settings.base_url.rstrip("/") if settings.base_url else str(request.base_url).rstrip("/")
    html = _html_template.replace("__BASE_URL__", base_url)
    return HTMLResponse(content=html)


@router.get("/health")
def health(request: Request):
    base_url = settings.base_url.rstrip("/") if settings.base_url else str(request.base_url).rstrip("/")
    return {
        "status":         "ok",
        "model":          settings.model_name,
        "version":        settings.version,
        "base_url":       base_url,
        "uptime_seconds": round(time.time() - START_TIME, 2),
    }
