# main.py 수정
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from audio_processor import AudioProcessor
from summary_service import SummaryService
from pydantic import BaseModel
from typing import List, Tuple
from score_annotator import parse_annotations
import librosa
import io
import logging

test_data = {
  "speech_segments": [
    {
      "start": 0,
      "end": 22.08,
      "text": "자 바요일린 음악 레슨 시작하겠습니다 오늘의 곡은 이제 찰고스 기회 꽃의 월치를 하려고 하는데 오늘 레슨의 전체적인 목표는 이제 고개의 리듬감과 우와함을 표현하는 것을 목표로 한번 해볼게요 일단 처음부터 끝까지 한번 연재해볼까요?"
    },
    {
      "start": 119.03999999999999,
      "end": 155.04,
      "text": "아, 근데 잘 들었어요. 아, 근데 지금은 조금 음악이 좀 단조로운 느낌이 드네. 그래서 이제 우리 1 마디부터 같이 해볼 거예요. 1 마디부터 같이 연재 봅시다. 일단 1 마디를 보면 이제 솔도 미 이제 세 가지 육이 있죠. 이 세음이 열인 내기로 연주해야 돼요. 너무 힘주지 말고 이마디를 향해서 부드러워게 진입한다는 느낌으로 해볼게요. 한번 해볼까요? 1 마디 1 마디 하면 연재 봅시다."
    },
    {
      "start": 159.84,
      "end": 162.23999999999998,
      "text": "한 번만 더 해볼까?"
    },
    {
      "start": 165.12,
      "end": 177.6,
      "text": "좋아요 그래서 이제 이맛일을 볼게요 이맛이부터 오마니가 이제 주제에요 왈채 기봉 강세야 할죠? 공작작작 이렇게 하듯이"
    },
    {
      "start": 178.07999999999998,
      "end": 181.92,
      "text": "2마디 첫받과 3마디 첫받에"
    },
    {
      "start": 182.88,
      "end": 193.92,
      "text": "풍! 이 다음보를 사용해서 이마디와 사마��에 다음보로 시작해서 리듬의 중심을 잡은 거예요 한번 해볼까요 이마디부터 일마디부터 오마��까지 한번 해볼까요?"
    },
    {
      "start": 206.4,
      "end": 235.2,
      "text": "네 좋아요 이제 육마기부터 구맛을 보면 앞에 이마디와 오마니 사이에 똑같은 멜로디가 반복되죠 여기서는 애코 효과를 주는 거예요 그래서 마치 매아리처럼 연주해서 다시 칠 마디를 약간 매아리처럼 연주해서 육마게 입체감을 더아리지 않는 거예요 알았죠 그러면 오마니부터 구맛이까지 한번 연주해볼까요"
    },
    {
      "start": 248.16,
      "end": 276.48,
      "text": "네 좋아요. 신마디를 보면 여기부터 이제 리듬이 바뀌죠. 신마디의 이제 이제 1, 2, 3, 4, 5, 6, 7, 8, 9. 신마디의 이제 이제 이제 첫음을 첫음이 이게 시 첫음 시일을 충분히 눌러주고 그 다음에 도도를 이제 약간 힘을 빼서 가볍게 마치 세가 총 총 띄어가는 느낌으로 신마디 해주면 되고"
    },
    {
      "start": 276.96,
      "end": 282.71999999999997,
      "text": "이렇게 한번 그럼 1마디부터 우리 여기 15마디까지만 해볼까요?"
    },
    {
      "start": 308.15999999999997,
      "end": 315.36,
      "text": "아 좋아요 그 신마디까지 잘했고 아 근데 학생 오늘 점심을 못 먹었어요"
    },
    {
      "start": 316.8,
      "end": 364.32,
      "text": "김치찌개 아 그렇죠 아 내가 대학교 때 학상 공부하는 시절에 나 때는 요즘 애들이 안 그랬는데 요즘 애들이 끊기다고 뭐라 해야 되지 김치찌개도 좋지만 김치찌개 저조찬말이야 근데 요즘 애들이 그런가 안 먹어요 그래서 학생은 조금 더 이제 연주 말고 외적인 부분에서도 밥을 듣는하게 먹어야 돼요 한국에는 밥심이란 말이야 그러니까 멘 같은 거 먹지 말고 밀가루 먹지 말고 요즘 나오는 거 뭐예요 재로시고 있잖아요 그러는 걸 이제 좀 많이 먹으세요 아시죠 그러니까 이제 우리 13아D까지 우리가 한 번 봤을 때 13아D에 저기 도샷 보이죠 도샷"
    },
    {
      "start": 365.28,
      "end": 381.59999999999997,
      "text": "도사별 운전이 불안해서 2번 손가락이 좀 낮게 잡히는 정상이 있어요. 그래서 이제 3번 손가락이 조금 더 가까이 깝게. 다시 13rd이의 손가락을 정확히 집는데 라는 걸 좀 기억을 해주시고"
    },
    {
      "start": 382.08,
      "end": 407.52,
      "text": "그럼 이제 17마디 20마디를 보면은 17마디부터 이제 이렇게 분위기가 바꾸니까 17마디를 레가 토로 연주해야 돼요 그리고 18마디부터 갈 때는 부드러운 비바블라토를 비브라토를 조금 더 넣을 거에요 이렇게 써야 됩니다 아시겠죠? 그러면 우리 20마디까지 하면 연주해볼까요? 13마디보다 20마디까지 하면 연주해볼까요?"
    },
    {
      "start": 437.28,
      "end": 443.52,
      "text": "아 좋아요 이 꽃의 왈치를 드리니까 내가 어렸을 때가 생각나는데"
    },
    {
      "start": 444,
      "end": 450.71999999999997,
      "text": "내가 어렸을 때 꽃을 참 좋아했어요. 나한테 이제 서정적인 사람인거든."
    },
    {
      "start": 451.2,
      "end": 499.68,
      "text": "그래서 곧 중에 이제 출립을 제일 좋아했는데 내달란다가 또 출립이 고향을 알죠 내달란 또 출립 고향이야 그래서 내달란 두 쪽에 이제 내가 살았을 때 이제 출립을 아침 전역으로 먹었어요 그 이게 또 가식이거든 이게 사람들은 많이 먹어 그래서 그랬는데 아 그래서 다시 이제 레슨으로 돌아와서 보면은 전체적으로 지금 이거대에 얘기하면 월치의 리듬감이 부족해요 그러니까 이 내재대에 있는 그 국작작 그 월치에 리듬에 따라서 좀 해주는 게 좋고 그리고 강약 조절 있잖아요 지금 강약 조절이 좀 중요해 강약 조절을 조금 더 신경 써줬으면 좋겠고"
    },
    {
      "start": 500.64,
      "end": 507.35999999999996,
      "text": "그러면 이제 다시 꽃이 발치를 해볼 텐데"
    },
    {
      "start": 507.84,
      "end": 537.12,
      "text": "23화 뒤에 보면 23화 뒤에 가장 높은 의미 있잖아요. 23화 뒤에 가장 높은 의미는 상의 크레스도 했다가 내려오는 작은 언덕을 만들어주세요. 그래서 감정을 표현해 주면 됩니다. 감정을 표현해 주면 되고. 그래서 이제 23화 뒤에 그렇게 하고 26마다리부터 30마다리까지 점점 소리를 키워가게 작성해 주세요. 23마다리부터"
    },
    {
      "start": 537.6,
      "end": 578.4,
      "text": "26만이 미안해 26만이 30만이까지 점점 소리가 커져야 되는 거고 31만이 보면 8분은 표가 나오죠. 30만이 8분은 표는 무겁지 않게 탭타시의 활수임으로 해주세요. 가볍고 경쾌하게 그리고 39만이에는 조금 더 부드럽게. 39만이 좀 더 부드럽게 해주시고 그리고 이제 도도리표 있죠. 도도리표의 위쪽에 도도리표 강조하는 표시이. 도도리표 강조 라고 써주시고 도도리표 강조는 몇만지 31만 23만이 도도리표 강조 써주고 이제 마지막으로 48만이 이제 마지막으로 늘릴 좀 여유롭게. 많이 3만 더 해봅시다. 그럼 처음부터 끝까지 다시 한번 연주해볼까요?"
    },
    {
      "start": 578.88,
      "end": 580.3199999999999,
      "text": "하면icia- Agriculture"
    },
    {
      "start": 652.3199999999999,
      "end": 709.4399999999999,
      "text": "아 좋아요. 여기까지 쓰면 좋고 마지막으로 체크할게 이제 31마디의 탭타세에 이제 23마디의 이제 통통 튀기 연주하기 일을 신경 쓰고 마지막으로 레슨을 종료하기 전에 새로운 과제를 내줄게요. 첫 번째 과제는 월치의 강세를 보면 이기는 겁니다. 쿵짝짝랑 상세에 대해 쿵짝짝랑 공작. 두 번째는 26마디의 에코피언이랑 23마디의 작은 언더표 연습해 보세요. 그리고 3번째 활드임. 자 17마디 레가토와 30마디 대타세의 늦쳐있더블 이해하시고 이제 각각의 맞는 화로 움직임을 써주시면 됩니다. 그리고 마지막 4번째 과제로는 13마디의 음정을 거울 보면서 송가락을 여기까지 정확하게 잡는 연습을 하면 됩니다. 이렇게 레슨 종료하도록 하겠습니다. 고생했어요."
    }
  ],
  "correctedTranscript": "자, 바이올린 음악 레슨을 시작하겠습니다. 오늘의 곡은 이제 찰스 기행 꽃의 왈츠를 하려고 하는데, 오늘 레슨의 전체적인 목표는 곡의 리듬과 우아함을 표현하는 것입니다. 먼저 처음부터 끝까지 한 번 연주해볼까요? 아, 잘 들었어요. 아, 지금은 조금 음악이 단조로운 느낌이 드네요. 그래서 이제 우리 1마디부터 같이 해볼 거예요. 1마디부터 같이 연주해봅시다. 먼저 1마디를 보면 솔, 미, 레, 시 세 음이 있어요. 이 세 음을 열심히 연주해야 돼요. 너무 힘줄 필요 없이 부드럽게 연주해보세요. 한 번 해볼까요? 1마디씩 하면서 연주해봅시다. 한 번 더 해볼까요? 좋아요. 그러면 이제 2마디를 볼게요. 2마디부터 오마니까지 연주해볼까요? 네, 좋아요. 이제 6마디를 보면 앞에 2마디와 오마니 사이에 같은 멜로디가 반복되요. 여기서는 애코 효과를 주는 거예요. 그래서 약간 매혹적으로 연주해서 다시 6마디를 약간 매혹적으로 연주해서 입체감을 더해주는 거예요. 알겠죠? 그러면 오마니부터 6마디까지 한 번 연주해볼까요? 네, 좋아요. 새로운 마디를 보면 여기부터 리듬이 바뀌죠. 새로운 마디에서 1, 2, 3, 4, 5, 6, 7, 8, 9. 새로운 마디에서 첫 음을 충분히 눌러주고, 다음에는 조금 가볍게 연주해주면 되요. 이렇게 하고 1마디부터 15마디까지 연주해볼까요? 아, 좋아요. 이 왈츠를 들으면 어릴 때가 생각나는데, 꽃을 참 좋아했어요. 나는 서정적인 사람이거든요. 그래서 특히 출립을 좋아했는데, 내 고향인 출립이야. 그래서 내 고향에선 출립을 아침, 저녁으로 먹었어요. 이게 사람들은 많이 먹어. 그래서 다시 레슨으로 돌아와서 보면, 전체적으로 월채의 리듬감이 부족해요. 그래서 국작작한 월채의 리듬에 맞춰 연주하는 게 좋고, 강약 조절도 중요해요. 강약 조절을 더 신경 써주면 좋겠어요. 그러면 다시 꽃의 왈츠를 연주해볼까요? 13마디부터 20마디까지 연주해볼까요? 아, 좋아요. 여기까지 쓰면 좋고, 마지막으로 체크할 건 31마디의 탭타시와 23마디의 작은 언더표 연주하기일을 신경 써주세요. 마지막으로 레슨을 종료하기 전에 새로운 과제를 내줄게요. 첫 번째 과제는 월채의 강세를 연습하는 거예요. 상세에 대해 연습해보세요. 두 번째는 26마디의 에코피언과 23마디의 작은 언더표 연습해 보세요. 세 번째는 17마디 레가토와 30마디 대타세의 늦쳐있는 더블 연습하고, 각각의 화로 움직임을 연습해주세요. 마지막으로 13마디의 음정을 거울 보면서 손가락을 정확하게 잡는 연습을 하세요. 이렇게 레슨을 종료하도록 하겠습니다. 수고하셨어요.",
  "summary": "## 총평 및 피드백 요약:\n- 오늘의 레슨 목표는 '찰스 기행 꽃의 왈츠' 곡의 리듬과 우아함을 표현하는 것이다.\n- 음악이 단조로운 느낌이 들어 조금 더 부드럽고 매혹적으로 연주하는 방법을 연습해야 한다.\n- 월채의 리듬과 강약 조절을 더 신경써야 한다.\n\n## 연주 기술 점검:\n- 1마디와 2마디, 그리고 6마디의 연주를 통해 음의 강세와 연주 방법을 연습했다.\n- 새로운 마디부터는 리듬이 바뀌며, 첫 음과 음악의 흐름을 조절하는 방법을 연습했다.\n- 꽃의 왈츠를 13마디부터 20마디까지 연주하며 강약 조절을 더 신경 써야 한다.\n- 탭타시와 작은 언더표 연주, 그리고 음정을 정확히 잡는 연습이 필요하다.\n\n## 마디별 주의사항:\n- 1마디부터 15마디까지: 음의 강세와 부드러움을 조절하며 연주해야 한다.\n- 13마디부터 20마디까지: 강약 조절에 더 신경써서 연주해야 한다.\n- 31마디와 23마디: 탭타시와 작은 언더표 연주에 집중해야 한다."
}

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
        audio_bytes = await file.read()
        waveform, sr = librosa.load(io.BytesIO(audio_bytes), sr=16000, mono=True)
        
        # 1. 음성 구간 추출 및 STT (원본 텍스트 생성)
        segments = audio_processor.extract_speech_segments(waveform, sr)
        logger.info("음성 구간 추출 완료")

        raw_speech_segments = audio_processor.transcribe_segments(segments, waveform, sr)
        logger.info("텍스트 변환 완료")

        # 2. STT 결과를 하나의 문자열로 합치고, ChatGPT로 보정
        raw_transcript = " ".join([seg["text"] for seg in raw_speech_segments])
        corrected_transcript = summary_service.correct_transcript(raw_transcript)
        logger.info("텍스트 보정 완료")

        # 3. 보정된 텍스트를 기반으로 요약 생성
        summary = summary_service.generate_summary(corrected_transcript)
        logger.info("레슨 내용 요약 완료")
        
        # 4. 클라이언트에 필요한 모든 정보를 담아 응답
        return JSONResponse(content={
            "speech_segments": raw_speech_segments,  # 원본 STT 결과
            "corrected_transcript": corrected_transcript, # 보정된 전체 텍스트
            "summary": summary # 요약
        })
        
        return JSONResponse(content=test_data)
        
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
    # 1. score_annotator는 튜플 리스트를 반환 -> 예: [(5, "빠르게"), (20, "부드럽게")]
    parsed_tuples: List[Tuple[int, str]] = parse_annotations(req.text)
    
    # 2. 튜플 리스트를 Pydantic 모델(AnnotationInfo) 객체 리스트로 변환
    #    - 각 튜플 (m, d)를 AnnotationInfo(measure=m, directive=d) 객체로 만듭니다.
    annotations_list = [AnnotationInfo(measure=m, directive=d) for m, d in parsed_tuples]
    logger.info("주석 파싱 완료")
    # 3. 최종적으로 Pydantic 모델로 감싸서 반환
    return AnnotationResponse(annotations=annotations_list)