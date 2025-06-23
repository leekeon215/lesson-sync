from openai import OpenAI, RateLimitError
from config import settings
import time

class SummaryService:
    def __init__(self):
        self.client = OpenAI(api_key=settings.OPENAI_API_KEY)

    def generate_summary(self, corrected_script):
        #texts = [seg["text"] for seg in segments if seg.get("text")]
        #transcript = "\n".join(texts)
        
        system_prompt = (
            "당신은 음악 레슨 내용을 전문적으로 요약하는 AI 어시스턴트입니다.\n\n"
            "입력되는 텍스트는 실제 레슨 현장에서 녹음된 음성을 변환한 스크립트입니다. "
            "따라서 일부 단어가 부정확하거나 문맥에 맞지 않을 수 있습니다. "
            "당신은 전체 대화가 '음악 레슨' 상황이라는 점을 반드시 인지하고, 대화의 본래 의도를 유추하며 요약해야 합니다.\n\n"
            "주어진 스크립트에서 다음의 규칙을 엄격히 준수하여 결과를 생성해주세요:\n"
            "1. 내용 선별: 오직 음악 레슨과 직접적으로 관련된 내용만 요약합니다. "
            "예를 들어, 연주 기술에 대한 피드백, 교사의 지시 사항, 연습 과제, 그리고 스타카토, 포르테, 포지션 이동과 같은 음악 전문 용어만 선별해야 합니다. "
            "일상적인 대화나 레슨과 무관한 잡담은 요약에서 완전히 제외합니다.\n"
            "2. 구조화: 요약 내용은 사용자가 쉽게 파악할 수 있도록 '총평 및 피드백 요약', '연주 기술 점검', '마디별 주의사항'과 같은 명확한 소제목으로 나누어 구조화해주세요.\n"
            "3. 언어: 최종 결과물은 반드시 한국어로 작성되어야 합니다."
        )

        user_prompt = (
            "[음악 레슨 스크립트]\n\n"
            f"{corrected_script}"
        )

        max_retries = 5
        base_delay = 1  # 초

        for attempt in range(max_retries):
            try:
                response = self.client.chat.completions.create(
                    model="gpt-3.5-turbo", # 또는 gpt-4-turbo
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": user_prompt}
                    ]
                )
                return response.choices[0].message.content
            
            except RateLimitError as e:
                # 속도 제한 오류가 발생했을 때
                if attempt < max_retries - 1:
                    # 대기 시간을 점차 늘림 (1초, 2초, 4초, ...)
                    delay = base_delay * (2 ** attempt)
                    print(f"Rate limit exceeded. Retrying in {delay} seconds... (Attempt {attempt + 1}/{max_retries})")
                    time.sleep(delay)
                else:
                    # 최대 재시도 횟수를 초과하면 최종적으로 오류를 발생시킴
                    print("Max retries reached. Failing.")
                    raise e
            except Exception as e:
                # 다른 종류의 오류는 즉시 발생시킴
                raise e
            
    def correct_transcript(self, transcript: str) -> str:
        """
        ChatGPT API를 사용하여 STT로 변환된 텍스트의 오타와 문맥을 보정합니다.
        """
        # 텍스트 보정을 위한 별도의 시스템 프롬프트
        correction_system_prompt = (
            "당신은 한국어 교정 전문가입니다. "
            "입력되는 텍스트는 음악 레슨 대화의 음성인식(STT) 결과입니다. "
            "이로 인해 오타, 띄어쓰기 오류, 문맥에 맞지 않는 단어(예: '사마디' -> '4마디', '비바블라토' -> '비브라토', '이맛이' -> '2마디')가 포함되어 있습니다. "
            "내용을 요약하거나 변경하지 말고, 오직 문법과 문맥에 맞게 오타와 오류만 수정하여 자연스러운 문장으로 된 전체 텍스트를 반환해 주세요. "
            "원문의 내용 중 음악 레슨과 관련 없는 내용은 생략합니다."
        )

        user_prompt = (
            "[원본 스크립트]\n\n"
            f"{transcript}"
        )
        
        # 재시도 로직은 간소화 (필요시 generate_summary와 동일하게 적용 가능)
        try:
            response = self.client.chat.completions.create(
                model="gpt-3.5-turbo", # 또는 gpt-4-turbo
                messages=[
                    {"role": "system", "content": correction_system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                temperature=0.2 # 더 사실에 가깝게 보정하도록 temperature 조정
            )
            return response.choices[0].message.content or transcript
        except Exception as e:
            print(f"Transcript correction failed: {e}")
            # 보정 실패 시 원본 텍스트를 그대로 반환
            return transcript