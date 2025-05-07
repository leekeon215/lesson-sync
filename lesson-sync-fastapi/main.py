# main.py 수정
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from audio_processor import AudioProcessor
from summary_service import SummaryService
import librosa
import io

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
