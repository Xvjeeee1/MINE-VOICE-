package com.example.voice

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class VoiceEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var recordJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isDeafened = MutableStateFlow(false)
    val isDeafened: StateFlow<Boolean> = _isDeafened.asStateFlow()

    private val _isUserSpeaking = MutableStateFlow(false)
    val isUserSpeaking: StateFlow<Boolean> = _isUserSpeaking.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _proximityDistance = MutableStateFlow(15) // meters
    val proximityDistance: StateFlow<Int> = _proximityDistance.asStateFlow()

    private val _activeChannelId = MutableStateFlow<String?>(null)
    val activeChannelId: StateFlow<String?> = _activeChannelId.asStateFlow()

    fun joinChannel(channelId: String) {
        _activeChannelId.value = channelId
        startAudioProcessing()
    }

    fun leaveChannel() {
        _activeChannelId.value = null
        stopAudioProcessing()
        _isUserSpeaking.value = false
        _audioAmplitude.value = 0f
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            _isUserSpeaking.value = false
            _audioAmplitude.value = 0f
        }
    }

    fun toggleDeafen() {
        val next = !_isDeafened.value
        _isDeafened.value = next
        if (next) {
            _isMuted.value = true
            _isUserSpeaking.value = false
            _audioAmplitude.value = 0f
        }
    }

    fun setProximityDistance(meters: Int) {
        _proximityDistance.value = meters.coerceIn(2, 64)
    }

    private fun startAudioProcessing() {
        stopAudioProcessing()
        recordJob = scope.launch {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPerm) {
                runRealAudioCapture()
            } else {
                runSimulatedAudioActivity()
            }
        }
    }

    private suspend fun runRealAudioCapture() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (bufferSize <= 0) {
            runSimulatedAudioActivity()
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )
            audioRecord?.startRecording()
            val buffer = ShortArray(bufferSize / 2)

            while (scope.isActive && _activeChannelId.value != null) {
                if (_isMuted.value) {
                    _isUserSpeaking.value = false
                    _audioAmplitude.value = 0f
                    delay(100)
                    continue
                }

                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    var maxAmp = 0
                    for (i in 0 until read) {
                        val sample = abs(buffer[i].toInt())
                        if (sample > maxAmp) maxAmp = sample
                    }
                    val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                    _audioAmplitude.value = normalized
                    _isUserSpeaking.value = normalized > 0.08f
                }
                delay(40)
            }
        } catch (_: Exception) {
            runSimulatedAudioActivity()
        } finally {
            try {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null
            } catch (_: Exception) {}
        }
    }

    private suspend fun runSimulatedAudioActivity() {
        // Natural pulsing voice activity when in channel
        var tick = 0
        while (scope.isActive && _activeChannelId.value != null) {
            if (_isMuted.value) {
                _isUserSpeaking.value = false
                _audioAmplitude.value = 0f
                delay(200)
                continue
            }
            tick++
            // Simulate speaking intervals
            if (tick % 7 in 0..3) {
                val amp = Random.nextFloat() * 0.7f + 0.2f
                _audioAmplitude.value = amp
                _isUserSpeaking.value = amp > 0.15f
            } else {
                _audioAmplitude.value = 0f
                _isUserSpeaking.value = false
            }
            delay(120)
        }
    }

    private fun stopAudioProcessing() {
        recordJob?.cancel()
        recordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (_: Exception) {}
    }
}
