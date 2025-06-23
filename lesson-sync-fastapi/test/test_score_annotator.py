import pytest
from score_annotator import parse_annotations, _convert_korean_to_int

# 숫자 변환 로직에 대한 테스트 케이스
@pytest.mark.parametrize("korean_number, expected_int", [
    ("삼십오", 35),
    ("열두", 12),
    ("스무", 20),
    ("열다섯", 15),
    ("이십삼", 23),
    ("5", 5),
    ("123", 123),
    ("없는숫자", 0),
])
def test_convert_korean_to_int(korean_number, expected_int):
    assert _convert_korean_to_int(korean_number) == expected_int

# 지시어 파싱 로직 테스트
def test_parse_annotations_korean_numbers():
    """한글 마디 번호 테스트"""
    text = "열다섯 번째 마디를 좀 더 강조해서 연주하고, 마디 스무에서는 여리게 해주세요."
    result = parse_annotations(text)
    result_dict = dict(result)

    assert 15 in result_dict
    assert 20 in result_dict
    assert "강조" in result_dict[15]
    # [수정] '여리게'의 원형인 '여리다'를 확인하도록 변경
    assert "여리다" in result_dict[20]