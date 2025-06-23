# config.py

import os
from pydantic_settings import BaseSettings, SettingsConfigDict
from dotenv import load_dotenv # 이 부분을 추가해주세요.

# --- 디버깅을 위한 코드 시작 ---
# .env 파일을 명시적으로 로드합니다.
load_dotenv() 

# 로드된 환경 변수 값을 직접 출력해봅니다.
# 터미널에 "sk-..." 형태의 키가 출력되어야 합니다.
print(f"Loaded OPENAI_API_KEY from .env: {os.getenv('OPENAI_API_KEY')}")
# --- 디버깅을 위한 코드 끝 ---


class Settings(BaseSettings):
    # .env 파일에서 OPENAI_API_KEY 라는 이름의 변수를 찾아 이 필드에 할당합니다.
    OPENAI_API_KEY: str

    # pydantic-settings가 .env 파일을 읽도록 설정합니다.
    model_config = SettingsConfigDict(
        env_file='.env', 
        env_file_encoding='utf-8',
        extra='ignore'
    )

settings = Settings()

print("Settings object created successfully!")


