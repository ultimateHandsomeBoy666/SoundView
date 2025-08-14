package com.soundwave

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.soundwave.soundwave.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            startRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
            }
        }
    }

    private fun startRecording() {
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = thread(start = true) {
            val buffer = ShortArray(bufferSize)
            var lastUpdateTime = 0L
            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    val currentTime = System.currentTimeMillis()
                    val gap = currentTime - lastUpdateTime
                    Log.e("niuwa", "currentTime - lastUpdateTime = ${gap}")
                    lastUpdateTime = currentTime
                    val audioData = buffer.copyOf(readSize)
                    val maxAmplitude = audioData.maxOrNull()?.toFloat() ?: 0f
                    val volume = calculateVolume(audioData)
                    runOnUiThread {
                        binding.voiceBubbleView.handleVolume(volume)
                        binding.volumeTextView.text = "Volume: $volume (Max: ${maxAmplitude.toInt()})\n bufferSize = $bufferSize"
                    }
                }
            }
        }
    }

    private fun calculateVolume(audioData: ShortArray): Int {
        if (audioData.isEmpty()) return 0
        
        // 计算RMS (Root Mean Square) 值
        var sum = 0.0
        for (sample in audioData) {
            sum += sample * sample
        }
        val rms = kotlin.math.sqrt(sum / audioData.size)
        
        // 将RMS值转换为0-35的范围（对应SoundWaveView的minVolume-maxVolume）
        val normalizedVolume = (rms / 32768.0 * 35).toInt()
        return normalizedVolume.coerceIn(0, 35)
    }

    override fun onStop() {
        super.onStop()
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        binding.voiceBubbleView.stopDance()
    }



}
