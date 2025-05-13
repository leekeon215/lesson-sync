from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from audio_processor import AudioProcessor
from summary_service import SummaryService
from score_annotator import annotate_score
import tempfile
import os

app = FastAPI(title="LessonSync FastAPI Server")
audio_processor = AudioProcessor()
summary_service = SummaryService()

@app.post("/lesson-summary")
async def process_lesson(file: UploadFile = File(...)):
    if not file.content_type.startswith("audio/"):
        raise HTTPException(400, "Only audio files allowed")
    
    with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as tmp:
        tmp.write(await file.read())
        tmp_path = tmp.name
        
    try:
        # 음성 구간 추출
        segments, waveform, sr = audio_processor.extract_speech_segments(tmp_path)
        
        # 텍스트 변환
        processed_segments = audio_processor.transcribe_segments(segments, waveform, sr)
        
        # 요약 생성
        summary = summary_service.generate_summary(processed_segments)
        
        return JSONResponse(content={
            "speech_segments": processed_segments,
            "summary": summary
        })
        
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={"message": f"처리 실패: {str(e)}"}
        )
    finally:
        os.remove(tmp_path)

# @app.post("/make-annotation")
# async def make_annotation():
    # annotations = parse_annotations("부드럽게 20번째 마디에서 시작해. 12 마디 빠르게.")

    # # 2) 원본 MusicXML 파일 경로
    # input_xml  = "input.musicxml"
    # # 3) 주석이 달린 결과물 경로
    # output_xml = "annotated_output.musicxml"

    # annotate_score(input_xml, annotations, output_xml)