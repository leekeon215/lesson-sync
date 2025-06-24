import re
from typing import List, Tuple
from konlpy.tag import Okt

# Okt 형태소 분석기 초기화
okt = Okt()

# 한글 숫자 변환 함수 (korean_number 라이브러리 대신 사용)
def korean_to_number(text: str) -> int:
    """한글 숫자를 아라비아 숫자로 변환"""
    
    # 기본 숫자 매핑
    basic_numbers = {
        '영': 0, '공': 0, '하나': 1, '한': 1, '일': 1, '둘': 2, '이': 2, '셋': 3, '세': 3, '삼': 3,
        '넷': 4, '네': 4, '사': 4, '다섯': 5, '오': 5, '여섯': 6, '육': 6, '일곱': 7, '칠': 7,
        '여덟': 8, '팔': 8, '아홉': 9, '구': 9
    }
    
    # 10의 배수 매핑
    tens = {
        '열': 10, '스무': 20, '스물': 20, '서른': 30, '마흔': 40, '쉰': 50,
        '예순': 60, '일흔': 70, '여든': 80, '아흔': 90
    }
    
    # 큰 단위 매핑
    big_units = {
        '십': 10, '백': 100, '천': 1000, '만': 10000
    }
    
    text = text.strip()
    
    # 특수 케이스 처리
    if text == '첫':
        return 1
    elif text == '두':
        return 2
    
    # 기본 숫자 직접 매핑
    if text in basic_numbers:
        return basic_numbers[text]
    
    # 10의 배수 직접 매핑
    if text in tens:
        return tens[text]
    
    # 큰 단위 직접 매핑
    if text in big_units:
        return big_units[text]
    
    # 복합 숫자 처리 (예: 스물세, 열다섯)
    for ten_key, ten_val in tens.items():
        if text.startswith(ten_key):
            remainder = text[len(ten_key):]
            if remainder in basic_numbers:
                return ten_val + basic_numbers[remainder]
    
    # 십의 배수 + 기본 숫자 (예: 십삼, 이십오)
    for basic_key, basic_val in basic_numbers.items():
        if basic_val > 0:  # 0은 제외
            for unit_key, unit_val in big_units.items():
                if unit_key == '십':
                    pattern = basic_key + unit_key
                    if text.startswith(pattern):
                        remainder = text[len(pattern):]
                        base_num = basic_val * unit_val
                        if remainder in basic_numbers:
                            return base_num + basic_numbers[remainder]
                        elif remainder == '':
                            return base_num
    
    # 처리되지 않은 경우
    raise ValueError(f"'{text}'를 숫자로 변환할 수 없습니다.")

# Okt 형태소 분석기 초기화
okt = Okt()

# '마디'를 찾고, 그 앞에 오는 텍스트를 non-greedy하게 캡처하는 정규식
# "번째"가 있어도 숫자 부분만 정확히 캡처
_MEASURE_RE = re.compile(r"(.+?)\s*(?:번째\s*)?마디")

# 지시어에서 제외할 불용어 리스트 (기존과 동일)
_STOP_WORDS = {'그리고', '그래서', '하지만', '그러나', '근데', '이제', '먼저', '음'}

def parse_annotations(text: str) -> List[Tuple[int, str]]:
    """
    [구 분할 오류 수정 버전]
    문장을 구 단위로 분해하는 로직을 개선하여, 하나의 문장에 여러 지시어가
    포함된 경우에도 정확하게 처리합니다.
    """
    annotations = []
    
    # [핵심 수정 1] 문장을 의미 단위(구)로 분할하는 정규식 개선
    # '하고'를 구분자로 추가하고, 쉼표와 마침표로도 분할
    clauses = re.split(r'\s*(?:[.,?!]|그리고|하고)\s*', text)
    print(f"분할된 구: {clauses}")  # 디버깅용 출력

    for clause in clauses:
        clause = clause.strip()
        # '마디'가 없는 구나 빈 구는 건너뜀
        if not clause or '마디' not in clause:
            continue
        
        match = _MEASURE_RE.search(clause)
        if match:            # '마디' 바로 앞 단어 그룹을 가져옵니다. (e.g., "세 번째", "스물세 번째")
            measure_str_raw = match.group(1).strip()
            
            # "번째" 제거 후 숫자 변환
            measure_str = re.sub(r'\s*번째\s*$', '', measure_str_raw).strip()
            print(f"원본: '{measure_str_raw}' -> 처리된 문자열: '{measure_str}'")  # 디버깅용 출력
            measure = None
            
            # 숫자 변환 로직
            if measure_str.isdigit():
                print(f"숫자 문자열: {measure_str}")  # 디버깅용 출력
                # 숫자 문자열을 정수로 변환
                measure = int(measure_str)
            else:
                # 특수 케이스 처리
                if measure_str == '첫':
                    measure = 1
                    print(f"특수 케이스 '첫': {measure}")
                elif measure_str == '두':
                    measure = 2
                    print(f"특수 케이스 '두': {measure}")
                else:
                    try:
                        measure = korean_to_number(measure_str)
                        print(f"korean_to_number 변환 성공: '{measure_str}' -> {measure}")
                    except (ValueError, KeyError) as e:
                        print(f"korean_to_number 변환 실패: '{measure_str}' -> 오류: {e}")
                        # 역순으로 단어를 조합해서 숫자 찾기
                        words = measure_str.split()
                        for j in range(len(words)):
                            candidate = " ".join(words[len(words) - 1 - j:])
                            try:
                                measure = korean_to_number(candidate)
                                print(f"역순 조합 성공: '{candidate}' -> {measure}")
                                break
                            except (ValueError, KeyError):
                                continue
            
            print(f"최종 measure 값: {measure}")
            if measure is None:
                print("measure가 None이므로 건너뜀")
                continue
                
            # 중복 방지
            if any(ann[0] == measure for ann in annotations):
                continue
                
            # [핵심 수정 2] '마디'와 그 뒤에 붙는 조사(은/는/이/가 등)까지 제거
            # '마디' 패턴이 끝나는 지점부터 텍스트를 잘라냅니다.
            directive_clause = clause[match.end():].strip()
              # 조사 제거 로직 개선
            if directive_clause:
                # 한국어 조사 목록 (직접 정의)
                josa_list = ['은', '는', '이', '가', '을', '를', '에', '에서', '로', '으로', '와', '과', '의', '도', '만', '부터', '까지', '에게', '께', '한테', '에다', '에다가']
                
                # 첫 번째 글자부터 조사 확인
                removed_josa = False
                for josa in sorted(josa_list, key=len, reverse=True):  # 긴 조사부터 확인
                    if directive_clause.startswith(josa):
                        directive_clause = directive_clause[len(josa):].strip()
                        print(f"조사 '{josa}' 제거됨, 남은 텍스트: '{directive_clause}'")
                        removed_josa = True
                        break
                
                if not removed_josa:
                    print(f"조사 제거 안됨, 원본 텍스트: '{directive_clause}'")

            # 지시어에서 의미 있는 단어 추출
            if directive_clause:
                tokens = okt.pos(directive_clause)
                meaningful_words = []
                for word, pos in tokens:
                    if pos not in ['Number', 'Josa', 'Conjunction', 'Suffix', 'Punctuation', 'Foreign'] and word not in ['마디', '번째']:
                        if word not in _STOP_WORDS:
                            meaningful_words.append(word)
                
                directive = " ".join(meaningful_words).strip()
                
                if directive:
                    annotations.append((measure, directive))

    return sorted(annotations, key=lambda x: x[0])

# # --- 테스트 코드 ---
# if __name__ == '__main__':
#     test_sentence_1 = "세 번째 마디는 부드럽게 연주하고 일곱번째 마디는 빠르게 연주해줘."
#     test_sentence_2 = "열다섯 마디는 아주 크게, 그리고 스물세 번째 마디는 작게."
#     test_sentence_3 = "첫 마디부터 시작하고, 두번째 마디에서 크레센도."
#     test_sentence_4 = "백 마디는 fff(포르티시시모)로 연주합니다."

#     print(f"입력: {test_sentence_1}")
#     print(f"결과: {parse_annotations(test_sentence_1)}")
#     # 예상 결과: [(3, '부드럽게 연주'), (7, '빠르게 연주 해줘')]

#     print("-" * 20)
#     print(f"입력: {test_sentence_2}")
#     print(f"결과: {parse_annotations(test_sentence_2)}")
#     # 예상 결과: [(15, '아주 크게'), (23, '작게')]

#     print("-" * 20)
#     print(f"입력: {test_sentence_3}")
#     print(f"결과: {parse_annotations(test_sentence_3)}")
#     # 예상 결과: [(1, '부터 시작'), (2, '에서 크레센도')]

#     print("-" * 20)
#     print(f"입력: {test_sentence_4}")
#     print(f"결과: {parse_annotations(test_sentence_4)}")
#     # 예상 결과: [(100, 'fff 포르티시시모 로 연주 합니다')]