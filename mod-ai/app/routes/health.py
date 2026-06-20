import time
from pathlib import Path
from fastapi import APIRouter
from fastapi.responses import HTMLResponse
from app.config import settings

router = APIRouter()

START_TIME = time.time()

_html = (Path(__file__).parent.parent / "static" / "index.html").read_text()


@router.get("/", response_class=HTMLResponse)
def root():
    return HTMLResponse(content=_html)


@router.get("/health")
def health():
    return {
        "status":         "ok",
        "model":          settings.model_name,
        "version":        settings.version,
        "uptime_seconds": round(time.time() - START_TIME, 2),
    }
