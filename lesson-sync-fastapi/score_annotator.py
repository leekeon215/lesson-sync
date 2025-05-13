import io
import os
import re
from typing import List, Tuple
from konlpy.tag import Okt
from music21 import converter, expressions, stream

okt = Okt()

_MEASURE_RE = re.compile(r'(\d+)\s*(?:번째\s*)?마디(?:에|에서|의)?')

def parse_annotations(text: str) -> List[Tuple[int, str]]:
    annotations = []
    seen = set()

    for m in _MEASURE_RE.finditer(text):
        measure = int(m.group(1))
        if measure in seen:
            continue
        seen.add(measure)

        # suffix: “마디” 패턴 바로 뒤부터 쉼표/마침표 전까지
        suffix = re.split(r'[,。.?!]', text[m.end():], maxsplit=1)[0].strip()
        tokens = okt.pos(suffix)
        adv_adj = [w for w, p in tokens if p in ('Adverb', 'Adjective')]

        if adv_adj:
            directive = ''.join(adv_adj)
        else:
            # prefix: “마디” 패턴 바로 앞 문장 조각
            prefix = re.split(r'[,。.?!]', text[:m.start()])[-1].strip()
            tokens2 = okt.pos(prefix)
            adv_adj2 = [w for w, p in tokens2 if p in ('Adverb', 'Adjective')]
            directive = ''.join(adv_adj2) if adv_adj2 else suffix

        annotations.append((measure, directive))

    return sorted(annotations, key=lambda x: x[0])


def annotate_score(
    annotations: List[Tuple[int, str]],
    xml_path: str
) -> str:
    
    if not os.path.isfile(xml_path):
        raise FileNotFoundError(f"Input MusicXML file not found: {xml_path}")

    score: stream.Score = converter.parse(xml_path)

    #주석 삽입
    for part in score.parts:
        measures = part.getElementsByClass('Measure')
        for measure_num, directive in annotations:
            idx = measure_num - 1
            if 0 <= idx < len(measures):
                measures[idx].insert(0.0, expressions.TextExpression(directive))

    buf = io.BytesIO()
    score.write('musicxml', fp=buf)
    xml_bytes = buf.getvalue()

    return xml_bytes.decode('utf-8')
