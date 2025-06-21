# main.py 수정
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from audio_processor import AudioProcessor
from summary_service import SummaryService
from pydantic import BaseModel
from typing import List
from score_annotator import parse_annotations
import librosa
import io
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="LessonSync FastAPI Server")
audio_processor = AudioProcessor()
summary_service = SummaryService()

@app.post("/lesson-summary")
async def process_lesson(file: UploadFile = File(...)):
    if not file.content_type.startswith("audio/"):
        raise HTTPException(400, "Only audio files allowed")

    try:
        # 메모리에서 오디오 처리
        audio_bytes = await file.read()
        waveform, sr = librosa.load(io.BytesIO(audio_bytes), sr=16000, mono=True)
        
        # 음성 구간 추출
        segments = audio_processor.extract_speech_segments(waveform, sr)
        
        # 텍스트 변환
        processed_segments = audio_processor.transcribe_segments(segments, waveform, sr)
        
        # 요약 생성
        #summary = summary_service.generate_summary(processed_segments)
        
        return JSONResponse(content={
            "speech_segments": processed_segments,
            #"summary": summary
        })
        
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"message": f"처리 실패: {str(e)}"}
        )
    finally:
        logger.info("레슨 요약 완료")

# --- API 요청/응답 Body를 위한 Pydantic 모델 ---
class AnnotationRequest(BaseModel):
    text: str

class AnnotationInfo(BaseModel):
    measure: int
    directive: str

class AnnotationResponse(BaseModel):
    annotations: List[AnnotationInfo]

@app.post("/parse-directives", response_model=AnnotationResponse)
async def parse_directives_from_text(req: AnnotationRequest):
    # api 테스트 중엔 주석 처리
    if not req.text:
        raise HTTPException(status_code=400, detail="Text cannot be empty") 
    
    # 이미 구현된 파싱 함수를 호출
    logger.info("주석 파싱 시작")
    annotations = parse_annotations(req.text)
    
    # 파싱된 결과를 JSON으로 반환
    # 예: [{"measure": 5, "directive": "빠르게"}]
    
    logger.info("주석 파싱 완료")
    return {"annotations": annotations}