import os
from pydantic_settings import BaseSettings

def _resolve_base_url() -> str:
    if os.environ.get("BASE_URL"):
        return os.environ["BASE_URL"].rstrip("/")
    replit_domain = os.environ.get("REPLIT_DEV_DOMAIN") or os.environ.get("REPLIT_DOMAINS", "").split(",")[0].strip()
    if replit_domain:
        return f"https://{replit_domain}"
    return ""


class Settings(BaseSettings):
    admin_key: str = os.environ.get("ADMIN_KEY", "changeme-set-ADMIN_KEY-env-var")
    base_url: str = _resolve_base_url()
    mod_db_url: str = "sqlite:///./mod_ai.db"
    model_name: str = "mod-1.0"
    version: str = "1.0.0"

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()
