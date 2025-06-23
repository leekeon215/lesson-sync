import pytest
from fastapi.testclient import TestClient

# main.py에서 app 객체를 임포트합니다.
# pytest.ini 설정 덕분에 절대 경로 임포트가 가능합니다.
from main import app

@pytest.fixture(scope="module")
def client():
    """
    FastAPI 앱을 위한 TestClient를 생성하는 Pytest Fixture.
    API를 호출하는 모든 테스트에서 이 client를 사용하게 됩니다.
    """
    with TestClient(app) as test_client:
        yield test_client