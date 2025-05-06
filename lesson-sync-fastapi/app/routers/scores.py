# app/routers/scores.py
from fastapi import APIRouter, UploadFile, File
from app.models import UploadScoreResponse

router = APIRouter(prefix="/scores", tags=["scores"])

@router.post("/", response_model=UploadScoreResponse, status_code=201)
async def upload_score(file: UploadFile = File(...)):
    # 나중에 여기서 업로드 등 하면 됨 
    return UploadScoreResponse(score_id=1)
