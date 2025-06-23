import re
from typing import List, Tuple
from konlpy.tag import Okt

# Okt 형태소 분석기 초기화
okt = Okt()

# '마디 8'과 '8 마디'를 모두 찾을 수 있도록 정규식 확장
_MEASURE_RE = re.compile(r"(?:(\d+)\s*(?:번째\s*)?마디|마디\s*(\d+))")

# 지시어에서 제외할 불용어 리스트
_STOP_WORDS = {'그리고', '그래서', '하지만', '그러나', '근데', '이제', '먼저', '음'}

def parse_annotations(text: str) -> List[Tuple[int, str]]:
    """
    [최종 완성 버전]
    Okt 형태소 분석기를 사용하여, '마디'가 포함된 구문 전체를 분석하고
    불필요한 품사를 제거하여 가장 정확한 지시어를 추출합니다.

    Args:
        text (str): STT를 통해 변환된 전체 레슨 대화 텍스트

    Returns:
        List[Tuple[int, str]]: [(마디번호, 지시어), ...] 형식의 리스트
    """
    annotations = []
    
    clauses = re.split(r"[,.?!]", text)
    
    for clause in clauses:
        clause = clause.strip()
        if not clause:
            continue
            
        match = _MEASURE_RE.search(clause)
        
        if match:
            # 두 개의 캡처 그룹 중 숫자가 있는 그룹에서 값을 가져옴
            measure_str = match.group(1) or match.group(2)
            if not measure_str:
                continue
            measure = int(measure_str)
            
            if any(ann[0] == measure for ann in annotations):
                continue

            tokens = okt.pos(clause)
            
            meaningful_words = []
            for word, pos in tokens:
                if pos not in ['Number', 'Josa', 'Conjunction', 'Suffix', 'Punctuation', 'Foreign'] and word not in ['마디', '번째']:
                    if word not in _STOP_WORDS:
                        meaningful_words.append(word)

            directive = " ".join(meaningful_words).strip()
            
            if directive:
                annotations.append((measure, directive))

    return sorted(annotations, key=lambda x: x[0])