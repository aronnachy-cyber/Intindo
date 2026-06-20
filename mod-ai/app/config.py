import os
import secrets
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    admin_key: str = os.environ.get("ADMIN_KEY", secrets.token_hex(32))
    mod_db_url: str = "sqlite:///./mod_ai.db"
    model_name: str = "mod-1.0"
    version: str = "1.0.0"

    class Config:
        env_file = ".env"
        env_prefix = ""
        extra = "ignore"


settings = Settings()
