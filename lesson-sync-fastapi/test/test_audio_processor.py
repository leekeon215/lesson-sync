# lesson-sync-fastapi/test/test_audio_processor.py

import pytest
import numpy as np
from audio_processor import AudioProcessor # audio_processor.py 에서 클래스 또는 함수를 가져옵니다.

# AudioProcessor 클래스가 있다고 가정합니다.
# 실제 클래스/함수 구조에 맞게 수정해야 합니다.

@pytest.fixture
def audio_processor_instance():
    """
    테스트에 사용할 AudioProcessor 인스턴스를 생성하는 Fixture입니다.
    """
    return AudioProcessor()

def test_load_audio(audio_processor_instance):
    """
    오디오 파일을 성공적으로 로드하는지 테스트합니다.
    """
    # 테스트용 가짜 오디오 파일 생성
    # 실제로는 librosa 등으로 읽을 수 있는 유효한 형식의 짧은 wav 파일을 test/fixtures 디렉토리에 만들어두는 것이 좋습니다.
    fake_audio_path = "test_audio.wav" 
    # (위 test_api.py에서 만든 test.wav를 사용하거나 새로 만듭니다)
    
    # 이 함수가 (오디오 데이터, 샘플링 레이트) 튜플을 반환한다고 가정합니다.
    audio_data, sr = audio_processor_instance.load_audio(fake_audio_path)
    
    assert isinstance(audio_data, np.ndarray) # 오디오 데이터가 numpy 배열인지 확인
    assert sr > 0 # 샘플링 레이트가 유효한 값인지 확인

def test_process_pitch(audio_processor_instance):
    """
    오디오 데이터로부터 피치를 정확하게 처리하는지 테스트합니다.
    """
    # 테스트용 가짜 오디오 데이터 생성 (예: 440Hz 사인파)
    sr = 22050
    duration = 1
    frequency = 440.0
    t = np.linspace(0., duration, int(sr * duration))
    amplitude = np.iinfo(np.int16).max * 0.5
    data = amplitude * np.sin(2. * np.pi * frequency * t)
    
    # 피치 처리 함수를 호출합니다.
    # 이 함수가 피치 정보를 담은 리스트를 반환한다고 가정합니다.
    pitch_info = audio_processor_instance.process_pitch(data, sr)
    
    assert isinstance(pitch_info, list)
    # 처리된 피치 정보가 예상 범위 내에 있는지 등을 확인할 수 있습니다.
    # 예를 들어, 평균 피치가 440Hz에 가까운지 확인할 수 있습니다.
    
    # 이 부분은 audio_processor.py의 실제 로직에 따라 더 구체적인 assert 구문이 필요합니다.