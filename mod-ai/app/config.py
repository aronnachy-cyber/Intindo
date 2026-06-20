import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    admin_key: str = os.environ.get("ADMIN_KEY", "changeme-set-ADMIN_KEY-env-var")
    mod_db_url: str = "sqlite:///./mod_ai.db"
    model_name: str = "mod-1.0"
    version: str = "1.0.0"

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()
