import re
from typing import List, Tuple
from konlpy.tag import Okt

# Okt 형태소 분석기 초기화
okt = Okt()

# --- 숫자 변환 로직 시작 (이전과 동일) ---
_KOR_NUM_1 = {'한': 1, '두': 2, '세': 3, '네': 4, '다섯': 5, '여섯': 6, '일곱': 7, '여덟': 8, '아홉': 9}
_KOR_NUM_1.update({'일': 1, '이': 2, '삼': 3, '사': 4, '오': 5, '육': 6, '칠': 7, '팔': 8, '구': 9})
_KOR_NUM_10 = {'열': 10, '스무': 20, '서른': 30, '마흔': 40, '쉰': 50}
_KOR_NUM_10.update({'십': 10, '이십': 20, '삼십': 30, '사십': 40, '오십': 50, '육십': 60, '칠십': 70, '팔십': 80, '구십': 90})
_KOR_NUM_100 = {'백': 100}
_KOR_NUM_1000 = {'천': 1000}

def _convert_korean_to_int(word: str) -> int:
    """ '이십사', '백서른둘' 등 조합된 숫자 단어를 정수로 변환합니다. """
    if word.isdigit():
        return int(word)

    num = 0
    temp_word = word

    # 천 단위 처리
    if '천' in temp_word:
        parts = temp_word.split('천')
        prefix = parts[0]
        if not prefix:
            num += 1000
        elif prefix in _KOR_NUM_1:
            num += _KOR_NUM_1[prefix] * 1000
        temp_word = parts[1]

    # 백 단위 처리
    if '백' in temp_word:
        parts = temp_word.split('백')
        prefix = parts[0]
        if not prefix:
            num += 100
        elif prefix in _KOR_NUM_1:
            num += _KOR_NUM_1[prefix] * 100
        temp_word = parts[1]

    # 십 단위 처리
    for kor, val in _KOR_NUM_10.items():
        if kor in temp_word:
            num += val
            temp_word = temp_word.replace(kor, "", 1).strip()
            break
            
    # 일 단위 처리
    if temp_word in _KOR_NUM_1:
        num += _KOR_NUM_1[temp_word]
        
    # 단일 단어 처리 (예: '다섯', '열', '백')
    if num == 0:
        all_single_nums = _KOR_NUM_1 | _KOR_NUM_10 | _KOR_NUM_100 | _KOR_NUM_1000
        if word in all_single_nums:
            return all_single_nums[word]

    return num if num > 0 else 0
# --- 숫자 변환 로직 끝 ---

# 정규식 패턴 생성
_ALL_KOR_WORDS = list(_KOR_NUM_1.keys()) + list(_KOR_NUM_10.keys()) + list(_KOR_NUM_100.keys())
_ALL_KOR_WORDS.sort(key=len, reverse=True)
_KOREAN_NUMBER_REGEX = "|".join(_ALL_KOR_WORDS)
# [수정] "마디 8"과 "8 마디"를 모두 찾을 수 있는 정규식
_MEASURE_RE = re.compile(rf"(?:((?:\d+|{_KOREAN_NUMBER_REGEX})+)\s*(?:번째\s*)?마디|마디\s*((?:\d+|{_KOREAN_NUMBER_REGEX})+))")

def parse_annotations(text: str) -> List[Tuple[int, str]]:
    """
    [최종 디버깅 버전]
    문맥을 분리하고, 지시어 후보군에서 가장 적합한 단어들을 선택하여
    주석을 생성합니다.
    """
    annotations = []
    
    # 1. 문장 부호를 기준으로 문맥을 분리
    clauses = re.split(r"[,.?!]", text)
    
    for clause in clauses:
        clause = clause.strip()
        if not clause: continue
            
        match = _MEASURE_RE.search(clause)
        if not match: continue
            
        # 2. 마디 번호 추출
        number_word = match.group(1) or match.group(2)
        measure = _convert_korean_to_int(number_word)
        if measure == 0 or any(ann[0] == measure for ann in annotations):
            continue

        # 3. '마디' 키워드를 제외한 나머지 부분을 모두 지시어 후보로 설정
        directive_candidate = _MEASURE_RE.sub('', clause, 1).strip()
        
        # 4. 형태소 분석으로 불필요한 품사 제거
        tokens = okt.pos(directive_candidate, norm=True)
        
        meaningful_words = []
        for word, pos in tokens:
            # 명사, 동사, 형용사, 부사, 어근만 지시어로 간주
            if pos in ['Noun', 'Verb', 'Adjective', 'Adverb', 'Root']:
                meaningful_words.append(word)
        
        directive = " ".join(meaningful_words).strip()
        
        if directive:
            annotations.append((measure, directive))

    return sorted(annotations, key=lambda x: x[0])