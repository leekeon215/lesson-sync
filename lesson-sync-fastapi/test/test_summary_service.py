import pytest
from unittest.mock import MagicMock, patch
from openai import RateLimitError
from summary_service import SummaryService

@pytest.fixture
def mock_openai_client(mocker):
    """OpenAI 클라이언트를 모킹하는 Fixture"""
    mock_client = MagicMock()
    mocker.patch('summary_service.OpenAI', return_value=mock_client)
    return mock_client

def test_generate_summary_success(mock_openai_client):
    """요약 생성 성공 케이스 테스트"""
    # Arrange
    service = SummaryService()
    expected_summary = "This is a summary."
    
    # API 응답 모킹
    mock_response = MagicMock()
    mock_response.choices[0].message.content = expected_summary
    mock_openai_client.chat.completions.create.return_value = mock_response
    
    script = "This is a long script about a music lesson."
    
    # Act
    summary = service.generate_summary(script)
    
    # Assert
    assert summary == expected_summary
    mock_openai_client.chat.completions.create.assert_called_once()
    call_args = mock_openai_client.chat.completions.create.call_args
    assert "음악 레슨 내용을 전문적으로 요약" in call_args.kwargs['messages'][0]['content']
    assert script in call_args.kwargs['messages'][1]['content']

def test_correct_transcript_success(mock_openai_client):
    """스크립트 교정 성공 케이스 테스트"""
    # Arrange
    service = SummaryService()
    expected_correction = "이것은 교정된 스크립트입니다."

    # API 응답 모킹
    mock_response = MagicMock()
    mock_response.choices[0].message.content = expected_correction
    mock_openai_client.chat.completions.create.return_value = mock_response
    
    transcript = "이것은 교정이필요한 스크립트임니다."
    
    # Act
    corrected_text = service.correct_transcript(transcript)
    
    # Assert
    assert corrected_text == expected_correction
    mock_openai_client.chat.completions.create.assert_called_once()
    call_args = mock_openai_client.chat.completions.create.call_args
    assert "한국어 교정 전문가입니다" in call_args.kwargs['messages'][0]['content']
    assert transcript in call_args.kwargs['messages'][1]['content']