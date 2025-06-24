import tensorflow as tf
import tensorflow_hub as hub
import numpy as np
import pandas as pd
import whisper
import torch

class AudioProcessor:
    def __init__(self):
        # YAMNet 초기화
        self.yamnet_model = hub.load('https://tfhub.dev/google/yamnet/1')
        self.class_names = self._load_class_names()
        
        # Whisper 초기화
        self.whisper_model = self._init_whisper()

    def _load_class_names(self):
        class_map_path = self.yamnet_model.class_map_path().numpy().decode('utf-8')
        return list(pd.read_csv(class_map_path)['display_name'])

    def _init_whisper(self):
        device = "cuda" if torch.cuda.is_available() else "cpu"
        # print(f"device: {device}")
        return whisper.load_model("small", device=device)

    def extract_speech_segments(self, waveform, sr):
        waveform_tf = tf.convert_to_tensor(waveform, dtype=tf.float32)
        scores, _, _ = self.yamnet_model(waveform_tf)
        return self._process_scores(scores.numpy(), sr)

    def _process_scores(self, scores, sr):
        top_class_indices = np.argmax(scores, axis=1)
        top_classes = [self.class_names[i] for i in top_class_indices]
        
        frame_hop = 0.48
        segments = []
        cur_start = None
        
        for i, cls in enumerate(top_classes):
            if cls == "Speech":
                if cur_start is None:
                    cur_start = i * frame_hop
            else:
                if cur_start is not None:
                    segments.append({"start": cur_start, "end": i * frame_hop})
                    cur_start = None
        if cur_start is not None:
            segments.append({"start": cur_start, "end": len(top_classes)*frame_hop})
            
        return segments

    def transcribe_segments(self, segments, waveform, sr):
        for seg in segments:
            start_sample = int(seg['start'] * sr)
            end_sample = int(seg['end'] * sr)
            segment_audio = waveform[start_sample:end_sample]
            
            if len(segment_audio) < sr:
                continue
                
            audio_float32 = segment_audio.astype(np.float32)
            result = self.whisper_model.transcribe(audio_float32, language="ko")
            seg["text"] = result["text"].strip()
            
        return [seg for seg in segments if "text" in seg]
