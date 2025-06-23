package com.lessonsync.app.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.IOException

class AudioPlayer(private val context: Context) {

    private var player: MediaPlayer? = null
    var isPlaying: Boolean = false
        private set

    fun playFile(filePath: String, onCompletion: () -> Unit) {
        if (isPlaying) {
            stop()
        }

        val fileUri = filePath.toUri()
        MediaPlayer.create(context, fileUri).apply {
            player = this
            setOnCompletionListener {
                stop() // 재생 완료 시 stop() 호출
                onCompletion() // 콜백 실행
            }
            try {
                start()
                this@AudioPlayer.isPlaying = true
            } catch (e: IOException) {
                e.printStackTrace()
                // 파일 재생 오류 처리
                stop()
            }
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
        isPlaying = false
    }
}