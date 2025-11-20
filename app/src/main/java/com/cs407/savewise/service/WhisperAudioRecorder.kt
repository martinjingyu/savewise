package com.cs407.savewise.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import androidx.core.content.ContextCompat
import java.io.*

class WavAudioRecorder(
    private val context: Context,
    private val file: File,
    private val onError: (String) -> Unit = {}
) {

    private var recorder: AudioRecord? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val channels = AudioFormat.CHANNEL_IN_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT

    private var rawFile = File(file.parent, "temp_raw.pcm")

    fun start() {

        // 1. 权限检查
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            onError("No RECORD_AUDIO permission")
            return
        }

        // 2. 初始化 AudioRecord
        val minBuffer = AudioRecord.getMinBufferSize(sampleRate, channels, encoding)
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channels,
            encoding,
            minBuffer
        )

        if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
            onError("AudioRecord init failed")
            return
        }

        recorder?.startRecording()
        isRecording = true

        // 3. 写 PCM 数据
        Thread {
            try {
                val buffer = ByteArray(minBuffer)
                val os = FileOutputStream(rawFile)

                while (isRecording) {
                    val read = recorder!!.read(buffer, 0, buffer.size)
                    if (read > 0) os.write(buffer, 0, read)
                }

                os.close()

                // 4. PCM → WAV
                convertRawToWave(rawFile, file)

            } catch (e: Exception) {
                onError("Recording failed: ${e.message}")
            }
        }.start()
    }

    fun stop() {
        isRecording = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    private fun convertRawToWave(raw: File, wav: File) {
        val rawData = raw.readBytes()
        val totalDataLen = rawData.size + 36
        val totalAudioLen = rawData.size
        val longSampleRate = sampleRate.toLong()
        val channelsCount = 1
        val byteRate = 16 * sampleRate * channelsCount / 8

        val header = ByteArray(44)

        // WAV header
        writeWavHeader(
            header,
            totalAudioLen.toLong(),
            totalDataLen.toLong(),
            longSampleRate,
            channelsCount,
            byteRate.toLong()
        )

        val output = FileOutputStream(wav)
        output.write(header)
        output.write(rawData)
        output.close()

        raw.delete() // 删除临时 PCM
    }

    private fun writeWavHeader(
        header: ByteArray,
        totalAudioLen: Long,
        totalDataLen: Long,
        sampleRate: Long,
        channels: Int,
        byteRate: Long
    ) {
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        writeLong(header, 4, totalDataLen)

        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()

        writeInt(header, 16, 16)
        writeShort(header, 20, 1)
        writeShort(header, 22, channels.toShort())
        writeLong(header, 24, sampleRate)
        writeLong(header, 28, byteRate)
        writeShort(header, 32, (channels * 16 / 8).toShort())
        writeShort(header, 34, 16.toShort())

        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()

        writeLong(header, 40, totalAudioLen)
    }

    private fun writeInt(header: ByteArray, offset: Int, value: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = (value shr 8 and 0xff).toByte()
        header[offset + 2] = (value shr 16 and 0xff).toByte()
        header[offset + 3] = (value shr 24 and 0xff).toByte()
    }

    private fun writeLong(header: ByteArray, offset: Int, value: Long) {
        writeInt(header, offset, (value and 0xffffffffL).toInt())
    }

    private fun writeShort(header: ByteArray, offset: Int, value: Short) {
        header[offset] = (value.toInt() and 0xff).toByte()
        header[offset + 1] = (value.toInt() shr 8 and 0xff).toByte()
    }
}