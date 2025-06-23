import re
from typing import List, Tuple
from konlpy.tag import Okt

# Okt 형태소 분석기 초기화
okt = Okt()

def _convert_korean_to_int(word: str) -> int:
    """한글 또는 숫자로 된 문자열을 정수로 변환합니다. (수정된 최종본)"""
    word = word.strip().replace(' ', '')
    if word.isdigit():
        return int(word)

    # 모든 한글 숫자 단어를 포함하는 사전
    korean_num_dict = {
        '한': 1, '두': 2, '세': 3, '네': 4, '다섯': 5, '여섯': 6, '일곱': 7, '여덟': 8, '아홉': 9, '열': 10,
        '열한': 11, '열두': 12, '열세': 13, '열네': 14, '열다섯': 15, '열여섯': 16, '열일곱': 17, '열여덟': 18, '열아홉': 19,
        '스무': 20, '서른': 30, '마흔': 40, '쉰': 50,
        '일': 1, '이': 2, '삼': 3, '사': 4, '오': 5, '육': 6, '칠': 7, '팔': 8, '구': 9, '십': 10,
        '이십': 20, '삼십': 30, '사십': 40, '오십': 50, '육십': 60, '칠십': 70, '팔십': 80, '구십': 90
    }

    # 단일 단어로 바로 변환되는 경우 (예: '열두', '스무')
    if word in korean_num_dict:
        return korean_num_dict[word]

    # '십'이 포함된 복합어 처리 (예: '이십삼', '삼십오')
    total = 0
    if '십' in word:
        parts = word.split('십')
        head = parts[0]
        tail = parts[1]
        
        if head: # '이'십, '삼'십 ...
            if head in korean_num_dict:
                total += korean_num_dict[head] * 10
        else: # '십'삼, '십'오 ...
            total += 10
        
        if tail: # ...'삼', ...'오'
            if tail in korean_num_dict:
                total += korean_num_dict[tail]
        return total
    
    return 0 # 변환할 수 없는 경우

# --- 정규식 및 파싱 로직 수정 ---
_KOREAN_NUMBER_WORDS = ['한', '두', '세', '네', '다섯', '여섯', '일곱', '여덟', '아홉', '열', '스무', '서른', '마흔', '쉰', '일', '이', '삼', '사', '오', '육', '칠', '팔', '구', '십', '백', '천']
_KOREAN_NUMBER_REGEX = '|'.join(_KOREAN_NUMBER_WORDS)
_MEASURE_RE = re.compile(rf"(?:((?:\d+|{_KOREAN_NUMBER_REGEX})+)\s*(?:번째\s*)?마디|마디\s*((?:\d+|{_KOREAN_NUMBER_REGEX})+))")

def parse_annotations(text: str) -> List[Tuple[int, str]]:
    """
    전체 텍스트에서 '마디' 키워드를 찾고, 그 다음에 나오는 내용을 지시어로 파싱합니다.
    """
    annotations = []
    # 1. 텍스트 전체에서 '마디'와 관련된 모든 부분을 찾음
    matches = list(_MEASURE_RE.finditer(text))
    if not matches:
        return []

    for i, match in enumerate(matches):
        # 2. 마디 번호 추출 및 변환
        number_word = match.group(1) or match.group(2)
        measure = _convert_korean_to_int(number_word)
        if measure == 0 or any(ann[0] == measure for ann in annotations):
            continue

        # 3. 지시어 탐색 범위를 현재 마디와 다음 마디 사이로 설정
        directive_start = match.end()
        directive_end = matches[i+1].start() if (i + 1) < len(matches) else len(text)
        
        # 4. 후보 텍스트에서 핵심 지시어 추출
        directive_candidate = text[directive_start:directive_end].strip()
        if not directive_candidate:
            continue
            
        pos_result = okt.pos(directive_candidate, norm=True, stem=True)
        
        # [수정] 품사 필터에 'Noun'(명사)를 추가하여 '강조' 같은 단어를 포함시킵니다.
        directive_words = [word for word, pos in pos_result if pos in ['Noun', 'Adjective', 'Verb', 'Adverb']]
        
        final_directive = ' '.join(directive_words).strip()
        if final_directive:
            annotations.append((measure, final_directive))
            
    return annotations