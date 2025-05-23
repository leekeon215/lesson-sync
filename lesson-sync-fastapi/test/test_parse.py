import re
from typing import List, Tuple
from konlpy.tag import Okt

okt = Okt()

# 마디 번호만 잡아내는 정규식
_MEASURE_RE = re.compile(r'(\d+)\s*(?:번째\s*)?마디(?:에|에서|의)?')

def parse_annotations(text: str) -> List[Tuple[int, str]]:
    """
    text 안에서 ‘숫자 + 마디’ 위치를 찾고,
    • suffix(뒤) → 부사/형용사 토큰
    • 없으면 prefix(앞) → 부사/형용사 토큰
    순으로 지시어를 추출합니다.
    """
    annotations = []
    seen = set()

    for m in _MEASURE_RE.finditer(text):
        measure = int(m.group(1))
        if measure in seen:
            continue
        seen.add(measure)

        # 1) suffix: 마디 패턴 끝난 지점부터 쉼표·마침표 전까지
        suffix = re.split(r'[,。.?!]', text[m.end():], maxsplit=1)[0].strip()
        tokens = okt.pos(suffix)
        adv_adj = [w for w, p in tokens if p in ('Adverb', 'Adjective')]

        if adv_adj:
            directive = ''.join(adv_adj)

        else:
            # 2) suffix 없으면 prefix: 마디 패턴 바로 앞 문장 조각
            prefix = re.split(r'[,。.?!]', text[:m.start()])[-1].strip()
            tokens2 = okt.pos(prefix)
            adv_adj2 = [w for w, p in tokens2 if p in ('Adverb', 'Adjective')]
            directive = ''.join(adv_adj2) if adv_adj2 else suffix

        annotations.append((measure, directive))

    # 마디 순으로 정렬
    return sorted(annotations, key=lambda x: x[0])

# (입력 문자열, 기대 결과)
test_cases = [
    ("57번째 마디에 좀 더 부드럽게 연주해.",
     [(57, "부드럽게")]),
    ("12 마디 빠르게, 그리고 100번째 마디에서 천천히.",
     [(12, "빠르게"), (100, "천천히")]),
    ("마디 8에서 강하게",
     [(8, "강하게")]),
    ("여기는 지시문이 없습니다.",
     []),
    ("부드럽게 20번째 마디에서 시작해.",
     [(20, "부드럽게")]),
]

def run_tests():
    all_ok = True
    for text, expected in test_cases:
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
