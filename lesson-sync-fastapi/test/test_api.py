# lesson-sync-fastapi/test/test_api.py

from fastapi.testclient import TestClient
from app.main import app  # app.main에서 FastAPI app 객체를 가져옵니다.

client = TestClient(app)

def test_health_check():
    """
    '/' 엔드포인트가 정상적으로 200 OK 응답을 반환하는지 테스트합니다.
    """
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "Hello World"}

def test_upload_score():
    """
    '/scores' 엔드포인트에 악보 파일을 업로드하는 것을 테스트합니다.
    실제 파일 대신 테스트용 파일을 사용합니다.
    """
    # 테스트용 MusicXML 파일을 생성하거나 기존 테스트 파일을 사용합니다.
    # 이 예시에서는 간단한 문자열을 파일처럼 만들어 전송합니다.
    test_file_content = b'<?xml version="1.0" encoding="UTF-8"?><score-partwise><part-list><score-part id="P1"><part-name>Music</part-name></score-part></part-list><part id="P1"><measure number="1"><note><pitch><step>C</step><octave>4</octave></pitch><duration>4</duration><type>whole</type></note></measure></part></score-partwise>'
    
    with open("test.xml", "wb") as f:
        f.write(test_file_content)

    with open("test.xml", "rb") as f:
        response = client.post("/scores/", files={"file": ("test.xml", f, "application/xml")})
    
    assert response.status_code == 200
    # 응답 JSON 구조가 예상과 맞는지 확인합니다.
    # 예를 들어, 악보 ID나 파일 이름 등을 확인할 수 있습니다.
    assert "filename" in response.json()
    assert response.json()["filename"] == "test.xml"

def test_upload_recording_for_score():
    """
    특정 악보에 대한 음원 파일을 업로드하는 것을 테스트합니다.
    """
    score_id = 1  # 테스트를 위한 악보 ID (실제 DB에 존재하는 ID여야 함)
    
    # 테스트용 WAV 파일을 생성하거나 사용합니다.
    # 여기서는 간단한 바이트 데이터를 파일처럼 만듭니다.
    test_audio_content = b'RIFF\x00\x00\x00\x00WAVEfmt \x10\x00\x00\x00\x01\x00\x01\x00\x44\xac\x00\x00\x88X\x01\x00\x02\x00\x10\x00data\x00\x00\x00\x00'

    with open("test.wav", "wb") as f:
        f.write(test_audio_content)
        
    with open("test.wav", "rb") as f:
         response = client.post(f"/scores/{score_id}/recordings", files={"file": ("test.wav", f, "audio/wav")})

    # 이 엔드포인트는 비동기 처리를 시작하므로 202 Accepted를 기대할 수 있습니다.
    # 또는 처리 완료 후 결과를 반환한다면 200 OK일 수 있습니다. main.py 로직에 따라 달라집니다.
    # 여기서는 200을 가정하겠습니다.
    assert response.status_code == 200 
    assert "msg" in response.json()
    # "Annotation started" 와 같은 메시지를 확인할 수 있습니다.