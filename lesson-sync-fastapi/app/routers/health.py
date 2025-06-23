# app/routers/health.py
from fastapi import APIRouter
from app.models import HealthResponse

router = APIRouter(tags=["meta"])

@router.get("/health", response_model=HealthResponse)
async def health():
    return HealthResponse()
