import pytest
from unittest.mock import patch, MagicMock

# 참고: conftest.py의 client fixture가 자동으로 주입됩니다.

# main.py에 정의된 인스턴스 변수(summary_service, audio_processor)를 직접 patch합니다.
@patch('main.summary_service')
@patch('main.audio_processor')
@patch('librosa.load')
def test_lesson_summary_flow(mock_librosa_load, mock_audio_processor, mock_summary_service, client):
    """오디오 파일 처리 및 요약 엔드포인트의 정상 흐름을 테스트합니다."""
    # Arrange: 각 서비스가 반환할 모의(mock) 데이터를 설정합니다.
    mock_librosa_load.return_value = (MagicMock(), 16000)
    mock_audio_processor.extract_speech_segments.return_value = "mock_segments"
    mock_audio_processor.transcribe_segments.return_value = [{"text": "speech"}]
    
    mock_summary_service.correct_transcript.return_value = "corrected"
    mock_summary_service.generate_summary.return_value = "summary"
    
    # Act: 실제 API 경로인 "/lesson-summary"를 호출합니다.
    response = client.post(
        "/lesson-summary",
        files={"file": ("test.wav", b"fake audio data", "audio/wav")}
    )

    # Assert: 성공 응답(200)과 올바른 JSON 결과가 반환되었는지 확인합니다.
    assert response.status_code == 200
    
    expected_content = {
        "speech_segments": [{"text": "speech"}],
        "corrected_transcript": "corrected",
        "summary": "summary"
    }
    assert response.json() == expected_content


@patch('main.librosa.load', side_effect=Exception("Test error"))
def test_lesson_summary_processing_error(mock_librosa_load, client):
    """오디오 처리 중 예외 발생 시 500 에러 반환을 테스트합니다."""
    # Act: 실제 API 경로인 "/lesson-summary"를 호출합니다.
    response = client.post(
        "/lesson-summary",
        files={"file": ("test.wav", b"fake audio", "audio/wav")}
    )

    # Assert: 서버 내부 오류(500)가 정상적으로 반환되는지 확인합니다.
    assert response.status_code == 500
    assert "처리 실패: Test error" in response.json()["message"]