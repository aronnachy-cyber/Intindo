import secrets
import time
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session
from app.auth import get_admin_key
from app.database import get_db, APIKey

router = APIRouter()


class CreateKeyRequest(BaseModel):
    name: str


@router.post("/v1/keys")
def create_key(
    body: CreateKeyRequest,
    db: Session = Depends(get_db),
    _admin: str = Depends(get_admin_key),
):
    key_value = f"sk-mod-{secrets.token_urlsafe(32)}"
    record = APIKey(key=key_value, name=body.name)
    db.add(record)
    db.commit()
    db.refresh(record)
    return {
        "key":        record.key,
        "name":       record.name,
        "created_at": record.created_at.isoformat(),
        "is_active":  record.is_active,
        "note":       "Save this key — it will not be shown again.",
    }


@router.get("/v1/keys")
def list_keys(
    db: Session = Depends(get_db),
    _admin: str = Depends(get_admin_key),
):
    records = db.query(APIKey).all()
    return {
        "object": "list",
        "data": [
            {
                "id":             r.id,
                "name":           r.name,
                "key_preview":    r.key[:12] + "...",
                "is_active":      r.is_active,
                "total_requests": r.total_requests,
                "created_at":     r.created_at.isoformat(),
            }
            for r in records
        ],
    }


@router.delete("/v1/keys/{key_id}")
def revoke_key(
    key_id: int,
    db: Session = Depends(get_db),
    _admin: str = Depends(get_admin_key),
):
    record = db.query(APIKey).filter(APIKey.id == key_id).first()
    if not record:
        raise HTTPException(status_code=404, detail="Key not found")
    record.is_active = False
    db.commit()
    return {"message": f"Key '{record.name}' has been revoked."}
