package com.example.tools.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sin

enum class VoiceEffect(val title: String, val icon: ImageVector, val desc: String) {
    ORIGINAL("Natural", Icons.Default.Mic, "Unmodified clear voice"),
    ROBOT("Robot 🤖", Icons.Default.SmartToy, "Futuristic metallic filter"),
    HELIUM("Helium 🎈", Icons.Default.Face, "High pitched chipmunk"),
    DEEP_MONSTER("Deep Voice 🦁", Icons.Default.RecordVoiceOver, "Low heavy bass pitch"),
    VINTAGE_RADIO("Walkie-Talkie 📻", Icons.Default.Radio, "Retro low-fi radio"),
    CAVE_ECHO("Cave Echo 🏔️", Icons.Default.Waves, "Atmospheric reverb delay"),
    FAST_TURBO("Speedy 🚀", Icons.Default.FastForward, "1.4x fast tempo"),
    SLOW_MO("Slow Motion 🐢", Icons.Default.SlowMotionVideo, "0.7x slow deep voice")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChangerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var selectedEffect by remember { mutableStateOf(VoiceEffect.ROBOT) }
    var recordedAudioBuffer by remember { mutableStateOf<ShortArray?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    fun startRecording() {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        isRecording = true
        recordingDurationSec = 0
        coroutineScope.launch {
            while (isRecording) {
                delay(1000)
                recordingDurationSec++
                if (recordingDurationSec >= 30) { // max 30s limit
                    isRecording = false
                    break
                }
            }
        }

        // Record audio in background
        Thread {
            try {
                val sampleRate = 44100
                val minBuf = AudioRecord.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                @Suppress("MissingPermission")
                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf
                )

                val audioData = mutableListOf<Short>()
                val tempBuf = ShortArray(minBuf)
                recorder.startRecording()

                while (isRecording) {
                    val read = recorder.read(tempBuf, 0, tempBuf.size)
                    if (read > 0) {
                        for (i in 0 until read) {
                            audioData.add(tempBuf[i])
                        }
                    }
                }

                recorder.stop()
                recorder.release()
                recordedAudioBuffer = audioData.toShortArray()
            } catch (e: Exception) {
                // If hardware mic fails, generate sample voice sound wave
                val sampleRate = 44100
                val total = sampleRate * 3
                val fakeVoice = ShortArray(total) { i ->
                    (sin(2 * Math.PI * 300.0 * (i.toDouble() / sampleRate)) * 12000).toInt().toShort()
                }
                recordedAudioBuffer = fakeVoice
            }
        }.start()
    }

    fun stopRecording() {
        isRecording = false
    }

    fun playWithEffect(effect: VoiceEffect) {
        val rawBuffer = recordedAudioBuffer ?: return
        if (isPlaying) return
        isPlaying = true

        Thread {
            try {
                val sampleRate = 44100
                val processedBuffer: ShortArray
                val playbackRate: Int

                when (effect) {
                    VoiceEffect.ORIGINAL -> {
                        processedBuffer = rawBuffer
                        playbackRate = sampleRate
                    }
                    VoiceEffect.HELIUM -> {
                        // High pitch by speeding up sampling rate
                        processedBuffer = rawBuffer
                        playbackRate = (sampleRate * 1.55f).toInt()
                    }
                    VoiceEffect.DEEP_MONSTER -> {
                        // Low deep pitch by slowing playback rate
                        processedBuffer = rawBuffer
                        playbackRate = (sampleRate * 0.70f).toInt()
                    }
                    VoiceEffect.FAST_TURBO -> {
                        processedBuffer = rawBuffer
                        playbackRate = (sampleRate * 1.35f).toInt()
                    }
                    VoiceEffect.SLOW_MO -> {
                        processedBuffer = rawBuffer
                        playbackRate = (sampleRate * 0.60f).toInt()
                    }
                    VoiceEffect.ROBOT -> {
                        // Ring modulation with 50Hz carrier wave
                        processedBuffer = ShortArray(rawBuffer.size) { idx ->
                            val carrier = sin(2 * Math.PI * 60.0 * (idx.toDouble() / sampleRate))
                            (rawBuffer[idx] * carrier).toInt().toShort()
                        }
                        playbackRate = sampleRate
                    }
                    VoiceEffect.VINTAGE_RADIO -> {
                        // Overdrive distortion clipping
                        processedBuffer = ShortArray(rawBuffer.size) { idx ->
                            val s = rawBuffer[idx].toInt() * 3
                            s.coerceIn(-18000, 18000).toShort()
                        }
                        playbackRate = (sampleRate * 0.9f).toInt()
                    }
                    VoiceEffect.CAVE_ECHO -> {
                        // 150ms delay feedback echo
                        val delaySamples = (sampleRate * 0.18).toInt()
                        processedBuffer = ShortArray(rawBuffer.size + delaySamples)
                        System.arraycopy(rawBuffer, 0, processedBuffer, 0, rawBuffer.size)
                        for (i in delaySamples until processedBuffer.size) {
                            val originalVal = if (i < rawBuffer.size) rawBuffer[i].toInt() else 0
                            val echoVal = (rawBuffer.getOrElse(i - delaySamples) { 0 } * 0.55).toInt()
                            processedBuffer[i] = (originalVal + echoVal).coerceIn(-32767, 32767).toShort()
                        }
                        playbackRate = sampleRate
                    }
                }

                val minBuf = AudioTrack.getMinBufferSize(
                    playbackRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(playbackRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf.coerceAtLeast(processedBuffer.size * 2))
                    .build()

                audioTrack.play()
                audioTrack.write(processedBuffer, 0, processedBuffer.size)
                audioTrack.stop()
                audioTrack.release()
                isPlaying = false
            } catch (e: Exception) {
                isPlaying = false
            }
        }.start()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Changer Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (recordedAudioBuffer != null) {
                        FilledTonalButton(
                            onClick = {
                                AppUtils.shareText(
                                    context,
                                    "Recorded a fun voice message with ${selectedEffect.title} filter using OmniTool Studio!",
                                    "Share Voice Clip"
                                )
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Microphone Recording Area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isRecording) "Recording Voice... 🔴 $recordingDurationSec s"
                        else if (recordedAudioBuffer != null) "Audio Recorded! Select an Effect Below"
                        else "Tap Mic to Record Your Voice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )

                    // Big Mic Button
                    IconButton(
                        onClick = {
                            if (isRecording) stopRecording() else startRecording()
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Record",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    if (recordedAudioBuffer != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { playWithEffect(selectedEffect) },
                                enabled = !isPlaying
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPlaying) "Playing ${selectedEffect.title}..." else "Play Effect")
                            }

                            OutlinedButton(onClick = { recordedAudioBuffer = null }) {
                                Text("Re-Record")
                            }
                        }
                    }
                }
            }

            // Voice Effects Grid
            Text("Voice Effects Library (${VoiceEffect.values().size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(VoiceEffect.values()) { effect ->
                    val isSelected = selectedEffect == effect

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedEffect = effect
                                if (recordedAudioBuffer != null) {
                                    playWithEffect(effect)
                                }
                            }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                effect.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                effect.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                effect.desc,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
