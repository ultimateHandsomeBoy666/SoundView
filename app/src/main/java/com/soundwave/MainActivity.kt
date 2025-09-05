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
    private val dbCalculator = DBCalculator()
    private var lastRecordTime = 0L

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
        binding.bigSoundView.setIdleHeightGetter { x ->
            0.07f * x * x * (12 - x) + 4
        }

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
            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0 && System.currentTimeMillis() - lastRecordTime > 250L) {
                    lastRecordTime = System.currentTimeMillis()
                    val audioData = buffer.copyOf(readSize)
                    val maxAmplitude = audioData.maxOrNull()?.toFloat() ?: 0f
                    val volume = dbCalculator.calculateDB(audioData, audioData.size).toInt() + 10
                    runOnUiThread {
                        binding.soundWaveView.handleVolume(volume)
                        binding.bigSoundView.handleVolume(volume)
                        binding.volumeTextView.text = "Volume: $volume (Max: ${maxAmplitude.toInt()})\n bufferSize = $bufferSize"
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }



}
