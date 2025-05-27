package com.lessonsync.app.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class WavAudioRecorder(
    private val outputFile: File,
    private val sampleRate: Int = 16000,
    private val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    private val audioEncoding: Int = AudioFormat.ENCODING_PCM_16BIT
) {
    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun startRecording() {
        if (isRecording) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioEncoding
        ).coerceAtLeast(1024)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioEncoding,
                minBufferSize
            ).apply {
                startRecording()
                isRecording = true
            }

            startPcmRecording(minBufferSize)
        } catch (e: Exception) {
            throw AudioRecorderInitException("녹음기 초기화 실패", e)
        }
    }

    private fun startPcmRecording(bufferSize: Int) {
        recordingJob = coroutineScope.launch {
            val pcmFile = createTempPcmFile()

            try {
                FileOutputStream(pcmFile).use { outputStream ->
                    val buffer = ByteArray(bufferSize)
                    while (isRecording) {
                        val bytesRead = audioRecord!!.read(buffer, 0, bufferSize)
                        if (bytesRead > 0) outputStream.write(buffer, 0, bytesRead)
                    }
                }
                convertToWav(pcmFile)
            } finally {
                pcmFile.delete()
                cleanupResources()
            }
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        recordingJob?.cancel()
        audioRecord?.stop()
    }

    private fun createTempPcmFile(): File {
        return File(outputFile.parent, "${outputFile.nameWithoutExtension}_temp.pcm").apply {
            if (exists()) delete()
            createNewFile()
        }
    }

    private fun convertToWav(pcmFile: File) {
        try {
            val pcmData = pcmFile.readBytes()
            FileOutputStream(outputFile).use { fos ->
                writeWavHeader(fos, pcmData.size)
                fos.write(pcmData)
            }
        } catch (e: IOException) {
            throw AudioConversionException("WAV 변환 실패", e)
        }
    }

    private fun writeWavHeader(outputStream: FileOutputStream, dataLength: Int) {
        val channels = if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        val byteRate = sampleRate * channels * 2 // 16-bit = 2 bytes

        val header = ByteArray(44).apply {
            // RIFF 헤더
            System.arraycopy("RIFF".toByteArray(), 0, this, 0, 4)
            System.arraycopy((dataLength + 36).toLittleEndian(), 0, this, 4, 4)
            System.arraycopy("WAVE".toByteArray(), 0, this, 8, 4)

            // fmt 서브청크
            System.arraycopy("fmt ".toByteArray(), 0, this, 12, 4)
            System.arraycopy(16.toLittleEndian(), 0, this, 16, 4) // PCM 헤더 크기
            System.arraycopy(1.toShort().toLittleEndian(), 0, this, 20, 2) // PCM 형식
            System.arraycopy(channels.toShort().toLittleEndian(), 0, this, 22, 2)
            System.arraycopy(sampleRate.toLittleEndian(), 0, this, 24, 4)
            System.arraycopy(byteRate.toLittleEndian(), 0, this, 28, 4)
            System.arraycopy((channels * 2).toShort().toLittleEndian(), 0, this, 32, 2) // 블록 정렬
            System.arraycopy(16.toShort().toLittleEndian(), 0, this, 34, 2) // 비트 당 샘플

            // data 서브청크
            System.arraycopy("data".toByteArray(), 0, this, 36, 4)
            System.arraycopy(dataLength.toLittleEndian(), 0, this, 40, 4)
        }

        outputStream.write(header)
    }

    private fun cleanupResources() {
        audioRecord?.release()
        audioRecord = null
    }

    private fun Int.toLittleEndian(): ByteArray = byteArrayOf(
        (this and 0xFF).toByte(),
        ((this shr 8) and 0xFF).toByte(),
        ((this shr 16) and 0xFF).toByte(),
        ((this shr 24) and 0xFF).toByte()
    )

    private fun Short.toLittleEndian(): ByteArray = byteArrayOf(
        (this.toInt() and 0xFF).toByte(),
        ((this.toInt() shr 8) and 0xFF).toByte()
    )

    class AudioRecorderInitException(message: String, cause: Throwable) :
        Exception(message, cause)

    class AudioConversionException(message: String, cause: Throwable) :
        Exception(message, cause)
}
