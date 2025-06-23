import re
from typing import List, Tuple

try:
    from score_annotator import parse_annotations
except ImportError:
    import sys
    sys.path.append('.')
    from score_annotator import parse_annotations

# 최종 로직에 대한 기대 결과
test_cases = [
    ("57번째 마디에 좀 더 부드럽게 연주해.",
     [(57, "좀 더 부드럽게 연주 해")]),
    ("12 마디 빠르게, 그리고 100번째 마디에서 천천히.",
     [(12, "빠르게"), (100, "천천히")]),
    ("마디 8에서 강하게",
     [(8, "강하게")]),
    ("여기는 지시문이 없습니다.",
     []),
    ("부드럽게 20번째 마디에서 시작해.",
     [(20, "부드럽게 시작 해")]),
    ("3마디 스타카토 살려서.",
     [(3, "스타카토 살려서")])
]

def run_tests():
    all_ok = True
    print("--- 최종 완성된 `parse_annotations` 함수 테스트 시작 ---")
    for i, (text, expected) in enumerate(test_cases):
        result = parse_annotations(text)
        ok = result == expected
        print(f"입력: {text!r}")
        print(f"예상: {expected}, 결과: {result} → {'OK' if ok else 'FAIL'}\n")
        if not ok:
            all_ok = False
    if all_ok:
        print("🎉 모든 테스트 통과!")
    else:
        print("❌ 테스트 실패! 로직을 점검하세요.")

if __name__ == "__main__":
    run_tests()