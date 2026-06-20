from fastapi import HTTPException, Security, Depends
from fastapi.security import APIKeyHeader
from sqlalchemy.orm import Session
from app.database import get_db, APIKey
from app.config import settings

api_key_header = APIKeyHeader(name="Authorization", auto_error=False)


def get_api_key(
    authorization: str = Security(api_key_header),
    db: Session = Depends(get_db)
) -> str:
    if not authorization:
        raise HTTPException(status_code=401, detail="Missing Authorization header")

    token = authorization.removeprefix("Bearer ").removeprefix("bot ").strip()

    record = db.query(APIKey).filter(
        APIKey.key == token,
        APIKey.is_active == True
    ).first()

    if not record:
        raise HTTPException(status_code=401, detail="Invalid or revoked API key")

    record.total_requests += 1
    db.commit()

    return token


def get_admin_key(authorization: str = Security(api_key_header)) -> str:
    if not authorization:
        raise HTTPException(status_code=401, detail="Missing Authorization header")

    token = authorization.removeprefix("Bearer ").strip()

    if token != settings.admin_key:
        raise HTTPException(status_code=403, detail="Admin key required")

    return token
