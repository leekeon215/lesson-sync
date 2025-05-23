#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from score_annotator import parse_annotations, annotate_score

def main():
    # 1) 주석 입력용 텍스트
    text = "1번째 마디에 빠르게, 2번째 마디에 느리게."

    # 2) parse_annotations 결과 리스트 생성
    annotations = parse_annotations(text)

    # 3) 입력 .mxl 파일 경로 확인
    input_path = "./sampleFile/Spring-Four_seasons_vivaldi.mxl"
    
    # 4) annotate_score 호출 (bytes 반환)
    annotated_mxl = annotate_score(annotations, input_path)

    print(f"압축된 MXL 파일을 생성했습니다: {annotated_mxl}")

if __name__ == "__main__":
    main()