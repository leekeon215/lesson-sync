import io
import os
import re
from typing import List, Tuple
from konlpy.tag import Okt
from music21 import converter, expressions, stream

okt = Okt()

_MEASURE_RE = re.compile(r"(\d+)\s*(?:번째\s*)?마디(?:에|에서|의)?")

def parse_annotations(text: str) -> List[Tuple[int, str]]:
    """
    주어진 텍스트에서 마디 번호와 지시어(부사/형용사)를 추출하여
    (마디 번호, 지시어) 리스트로 반환합니다.
    """
    annotations: List[Tuple[int, str]] = []
    seen = set()

    for m in _MEASURE_RE.finditer(text):
        measure = int(m.group(1))
        if measure in seen:
            continue
        seen.add(measure)

        # 패턴 뒤 텍스트에서 쉼표나 마침표 전까지 추출
        suffix = re.split(r"[,。.?!]", text[m.end():], maxsplit=1)[0].strip()
        tokens = okt.pos(suffix)
        adv_adj = [w for w, p in tokens if p in ('Adverb', 'Adjective')]

        if adv_adj:
            directive = ''.join(adv_adj)
        else:
            # 패턴 앞 텍스트에서 마지막 문장 조각 추출
            prefix = re.split(r"[,。.?!]", text[:m.start()])[-1].strip()
            tokens2 = okt.pos(prefix)
            adv_adj2 = [w for w, p in tokens2 if p in ('Adverb', 'Adjective')]
            directive = ''.join(adv_adj2) if adv_adj2 else suffix

        annotations.append((measure, directive))

    return sorted(annotations, key=lambda x: x[0])


def annotate_score(
    annotations: List[Tuple[int, str]],
    xml_path: str
) -> str:
    """
    주석 리스트와 MusicXML(.xml 또는 .mxl) 파일 경로를 받아,
    압축된 MXL 포맷의 바이너리를 반환합니다.

    :param annotations: [(measure_number, directive_text), ...]
    :param xml_path: 입력 MusicXML(.xml 또는 .mxl) 파일 경로
    :return: 주석이 삽입된 .mxl 바이너리
    """
    if not os.path.isfile(xml_path):
        raise FileNotFoundError(f"Input MusicXML file not found: {xml_path}")

    # MusicXML(.xml/.mxl) 로드
    score: stream.Score = converter.parse(xml_path)

    # 주석 삽입
    for part in score.parts:
        measures = part.getElementsByClass('Measure')
        for measure_num, directive in annotations:
            idx = measure_num - 1
            if 0 <= idx < len(measures):
                te = expressions.TextExpression(directive)
                te.style.color = 'blue'
                measures[idx].insert(0.0, te)

    # 메모리 버퍼에 MXL 압축 포맷으로 쓰기
    # buf = io.BytesIO()
    base, _ = os.path.splitext(xml_path)
    buf = f"{base}_annotated.mxl"
    score.write('mxl', fp=buf)
    return buf
