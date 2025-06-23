import pytest
from unittest.mock import MagicMock, patch
import numpy as np

# 테스트 대상 모듈보다 먼저 모킹이 필요한 라이브러리를 임포트
with patch('tensorflow_hub.load'), \
     patch('whisper.load_model'), \
     patch('pandas.read_csv'), \
     patch('tensorflow.convert_to_tensor', side_effect=lambda x, **kwargs: x):
    from audio_processor import AudioProcessor


def test_process_scores_logic():
    """YAMNet 모델의 점수를 기반으로 음성 구간을 추출하는 로직을 테스트"""
    # Arrange
    # AudioProcessor의 __init__을 호출하지 않고 인스턴스 생성
    processor = AudioProcessor.__new__(AudioProcessor)
    
    # 클래스 이름 모킹 (Speech가 인덱스 1에 있다고 가정)
    processor.class_names = ["Music", "Speech", "Silence"]
    
    # 모델 점수 모킹: [Speech, Speech, Music, Speech, Speech]
    scores = np.zeros((5, 3))
    scores[0, 1] = 1.0
    scores[1, 1] = 1.0
    scores[2, 0] = 1.0
    scores[3, 1] = 1.0
    scores[4, 1] = 1.0
    
    frame_hop = 0.48 # 원본 코드의 값
    
    # Act
    segments = processor._process_scores(scores, sr=16000)
    
    # Assert
    # 예상 결과: (0s-0.96s), (1.44s-2.4s)
    expected_segments = [
        {"start": 0.0 * frame_hop, "end": 2 * frame_hop},
        {"start": 3 * frame_hop, "end": 5 * frame_hop}
    ]
    assert segments == expected_segments

def test_transcribe_segments(mocker):
    """음성 구간을 STT로 변환하는 기능 테스트"""
    # Arrange
    # AudioProcessor의 __init__을 호출하지 않고 인스턴스 생성
    processor = AudioProcessor.__new__(AudioProcessor)
    
    # Whisper 모델 모킹
    mock_whisper_model = MagicMock()
    mock_whisper_model.transcribe.return_value = {'text': 'transcribed text'}
    processor.whisper_model = mock_whisper_model
    
    segments = [{'start': 0.0, 'end': 2.0}, {'start': 3.0, 'end': 5.0}]
    sr = 16000
    waveform = np.random.randn(5 * sr) # 5초 분량의 임의의 오디오 데이터
    
    # Act
    results = processor.transcribe_segments(segments, waveform, sr)
    
    # Assert
    assert mock_whisper_model.transcribe.call_count == 2
    assert len(results) == 2
    assert results[0]['text'] == 'transcribed text'
    assert results[1]['start'] == 3.0