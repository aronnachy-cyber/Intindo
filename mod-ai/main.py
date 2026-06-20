import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.database import init_db
from app.routes import health, moderations, analyze, classify, models_route, keys

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(name)s] %(levelname)s: %(message)s",
)
logger = logging.getLogger("mod-ai")


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting Mod-1.0 AI Moderation API...")
    init_db()
    logger.info(f"Database initialized")
    logger.info(f"Admin key: {settings.admin_key}")
    logger.info("Mod-1.0 is ready.")
    yield
    logger.info("Shutting down Mod-1.0...")


app = FastAPI(
    title="Mod-1.0",
    description=(
        "**Mod-1.0** is an AI-powered moderation API built for bots.\n\n"
        "Detect toxicity, hate speech, harassment, spam, violence, self-harm, "
        "sexual content, and profanity — with confidence scores and action recommendations.\n\n"
        "## Authentication\n"
        "All endpoints (except `/` and `/health`) require an API key:\n"
        "```\nAuthorization: Bearer sk-mod-your-key-here\n```\n\n"
        "## Admin\n"
        "Key management endpoints require the admin key set via the `ADMIN_KEY` environment variable."
    ),
    version=settings.version,
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router)
app.include_router(moderations.router)
app.include_router(analyze.router)
app.include_router(classify.router)
app.include_router(models_route.router)
app.include_router(keys.router)
