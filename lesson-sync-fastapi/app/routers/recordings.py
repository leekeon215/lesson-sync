# app/routers/recordings.py
from fastapi import APIRouter, UploadFile, File
from app.models import UploadRecResponse

router = APIRouter(prefix="/recordings", tags=["recordings"])

@router.post("/", response_model=UploadRecResponse, status_code=201)
async def upload_recording(file: UploadFile = File(...)):
    return UploadRecResponse(recording_id=1)
