# app/models.py
from pydantic import BaseModel

class HealthResponse(BaseModel):
    status: str = "ok"

class UploadScoreResponse(BaseModel):
    score_id: int
    message: str = "received"

class UploadRecResponse(BaseModel):
    recording_id: int
    message: str = "received"
