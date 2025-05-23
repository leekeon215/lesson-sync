# main.py 수정
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from audio_processor import AudioProcessor
from summary_service import SummaryService
from summary_parsing import parse_annotations
from add_annotation import annotate_score
import tempfile
import os
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