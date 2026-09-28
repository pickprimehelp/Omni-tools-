package com.example.tools.video

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.AppUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class VideoClip(
    val id: String,
    val title: String,
    val uri: Uri? = null,
    val durationSeconds: Float = 3f,
    val bgGradient: List<Color> = listOf(Color(0xFF6B46C1), Color(0xFF319795))
)

enum class TransitionType(val label: String) {
    FADE("Fade"),
    SLIDE("Slide"),
    ZOOM("Zoom In"),
    FLASH("Flash"),
    DISSOLVE("Dissolve")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultClips = remember {
        mutableStateListOf(
            VideoClip("1", "Intro Scene", null, 3f, listOf(Color(0xFFFF512F), Color(0xFFDD2476))),
            VideoClip("2", "Highlight Cut", null, 3f, listOf(Color(0xFF8A2387), Color(0xFFE94057))),
            VideoClip("3", "Drop Beat", null, 3f, listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
            VideoClip("4", "Outro Vibe", null, 3f, listOf(Color(0xFF4776E6), Color(0xFF8E54E9)))
        )
    }

    var selectedTransition by remember { mutableStateOf(TransitionType.SLIDE) }
    var selectedMusic by remember { mutableStateOf("Beats") }
    var isPlayingMusic by remember { mutableStateOf(false) }
    var isPlayingVideo by remember { mutableStateOf(false) }
    var currentClipIndex by remember { mutableIntStateOf(0) }
    var captionText by remember { mutableStateOf("✨ Feel The Rhythm 🔥") }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var aspectRatio by remember { mutableStateOf("9:16") } // "9:16", "1:1", "16:9"

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val newClip = VideoClip(
                id = System.currentTimeMillis().toString(),
                title = "Clip ${defaultClips.size + 1}",
                uri = uri,
                durationSeconds = 3f
            )
            defaultClips.add(newClip)
        }
    }

    // Playback loop
    LaunchedEffect(isPlayingVideo, playbackSpeed, defaultClips.size) {
        while (isPlayingVideo && defaultClips.isNotEmpty()) {
            val clipDuration = (defaultClips[currentClipIndex % defaultClips.size].durationSeconds * 1000 / playbackSpeed).toLong()
            delay(clipDuration)
            currentClipIndex = (currentClipIndex + 1) % defaultClips.size
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            AppUtils.stopAudioBeat()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reels & Video Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            AppUtils.shareText(
                                context,
                                "Created a high-energy $aspectRatio reel with $selectedTransition transition and $selectedMusic audio sync using OmniTool Studio!",
                                "Share Reel Project"
                            )
                        }
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Video Preview Player Box
            val playerAspectRatio = when (aspectRatio) {
                "9:16" -> 0.58f
                "1:1" -> 1f
                else -> 1.77f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (defaultClips.isNotEmpty()) {
                    val activeClip = defaultClips[currentClipIndex % defaultClips.size]
                    if (activeClip.uri != null) {
                        AsyncImage(
                            model = activeClip.uri,
                            contentDescription = "Clip preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(activeClip.bgGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.MovieCreation,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    activeClip.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    // Caption Overlay
                    if (captionText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 28.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = captionText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Watermark / Aspect ratio badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$aspectRatio • ${currentClipIndex + 1}/${defaultClips.size}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Play / Pause overlay button
                IconButton(
                    onClick = { isPlayingVideo = !isPlayingVideo },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        if (isPlayingVideo) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            // Timeline Scrubbing & Clip Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Clips Timeline", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                FilledTonalButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Media", fontSize = 12.sp)
                }
            }

            // Horizontal clips strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                defaultClips.forEachIndexed { index, clip ->
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (currentClipIndex == index) 3.dp else 1.dp,
                                color = if (currentClipIndex == index) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { currentClipIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (clip.uri != null) {
                            AsyncImage(
                                model = clip.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(clip.bgGradient))
                            )
                        }
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Controls: Transition, Music, Speed, Ratio
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Audio Music Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BGM Music Sync", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = {
                                if (isPlayingMusic) {
                                    AppUtils.stopAudioBeat()
                                    isPlayingMusic = false
                                } else {
                                    isPlayingMusic = true
                                    AppUtils.playAudioBeat(selectedMusic) {
                                        isPlayingMusic = false
                                    }
                                }
                            }
                        ) {
                            Icon(
                                if (isPlayingMusic) Icons.Default.VolumeUp else Icons.Default.PlayCircle,
                                contentDescription = "Preview Music",
                                tint = if (isPlayingMusic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Music genres
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Beats", "Romantic", "Upbeat", "Lo-fi", "Cinematic").forEach { genre ->
                            FilterChip(
                                selected = selectedMusic == genre,
                                onClick = {
                                    selectedMusic = genre
                                    if (isPlayingMusic) {
                                        AppUtils.playAudioBeat(genre) { isPlayingMusic = false }
                                    }
                                },
                                label = { Text(genre, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Transition Selector
                    Text("Transition FX", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TransitionType.values().forEach { trans ->
                            FilterChip(
                                selected = selectedTransition == trans,
                                onClick = { selectedTransition = trans },
                                label = { Text(trans.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Aspect Ratio & Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("9:16", "1:1", "16:9").forEach { ratio ->
                                FilterChip(
                                    selected = aspectRatio == ratio,
                                    onClick = { aspectRatio = ratio },
                                    label = { Text(ratio, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { spd ->
                                FilterChip(
                                    selected = playbackSpeed == spd,
                                    onClick = { playbackSpeed = spd },
                                    label = { Text("${spd}x", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Caption text field
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        label = { Text("Text & Sticker Overlay") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}
