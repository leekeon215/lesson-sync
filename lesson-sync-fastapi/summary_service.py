from openai import OpenAI
from config import settings  # 위에서 정의한 Settings 인스턴스

class SummaryService:
    def __init__(self):
        self.client = OpenAI(api_key=settings.OPENAI_API_KEY)

    def generate_summary(self, segments):
        texts = [seg["text"] for seg in segments if seg.get("text")]
        transcript = "\n".join(texts)
        prompt = (
            "[음악 레슨 요약]\n"
            "음악 레슨과 관련된 피드백, 연주 기술, 연습 과제, 음악 용어만 요약해 주세요.\n\n"
            f"{transcript}"
        )
        response = self.client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "너는 음악 레슨 요약 전문가야."},
                {"role": "user", "content": prompt}
            ]
        )
        return response.choices[0].message.content
