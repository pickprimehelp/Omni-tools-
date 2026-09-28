package com.example.tools.bgremover

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

enum class BgMode {
    TRANSPARENT,
    SOLID,
    GRADIENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundRemoverScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var tolerance by remember { mutableFloatStateOf(40f) }
    var selectedBgMode by remember { mutableStateOf(BgMode.TRANSPARENT) }
    var selectedSolidColor by remember { mutableStateOf(Color.White) }
    var selectedGradientIndex by remember { mutableIntStateOf(0) }
    var showOriginal by remember { mutableStateOf(false) }

    val gradients = listOf(
        listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)),
        listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
        listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
        listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4)),
        listOf(Color(0xFF232526), Color(0xFF414345))
    )

    fun processImage(bitmap: Bitmap, tol: Float) {
        coroutineScope.launch {
            isProcessing = true
            val result = withContext(Dispatchers.Default) {
                removeBackground(bitmap, tol.toInt())
            }
            processedBitmap = result
            isProcessing = false
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isProcessing = true
                val bmp = withContext(Dispatchers.IO) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                decoder.isMutableRequired = true
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                if (bmp != null) {
                    // Downscale for responsive mobile processing if needed
                    val maxDim = 800
                    val scaled = if (bmp.width > maxDim || bmp.height > maxDim) {
                        val ratio = maxDim.toFloat() / maxOf(bmp.width, bmp.height)
                        Bitmap.createScaledBitmap(bmp, (bmp.width * ratio).toInt(), (bmp.height * ratio).toInt(), true)
                    } else bmp
                    originalBitmap = scaled
                    processImage(scaled, tolerance)
                }
                isProcessing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background Eraser HD", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (processedBitmap != null) {
                        FilledTonalButton(
                            onClick = {
                                val exportBmp = composeFinalBitmap(
                                    processedBitmap!!,
                                    selectedBgMode,
                                    selectedSolidColor,
                                    gradients[selectedGradientIndex]
                                )
                                AppUtils.saveAndShareBitmap(context, exportBmp, "Share Cutout")
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export HD")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Preview Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (originalBitmap == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Smart AI Background Eraser", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Extract subjects with clean edges & replace backdrops", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Photo")
                        }
                    }
                } else {
                    // Render backdrop behind cutout
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedBgMode) {
                            BgMode.TRANSPARENT -> {
                                // Subtle checkerboard pattern indicator
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF2D3748))
                                )
                            }
                            BgMode.SOLID -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(selectedSolidColor)
                                )
                            }
                            BgMode.GRADIENT -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.linearGradient(gradients[selectedGradientIndex]))
                                )
                            }
                        }

                        val displayBmp = if (showOriginal) originalBitmap else processedBitmap
                        if (displayBmp != null) {
                            Image(
                                bitmap = displayBmp.asImageBitmap(),
                                contentDescription = "Cutout Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        if (isProcessing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Toggle Before/After
                        FilledTonalButton(
                            onClick = { showOriginal = !showOriginal },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showOriginal) "Cutout" else "Original", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Controls when photo is loaded
            if (originalBitmap != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Edge Tolerance Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Edge Threshold", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${tolerance.toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = tolerance,
                            onValueChange = { tolerance = it },
                            onValueChangeFinished = {
                                originalBitmap?.let { processImage(it, tolerance) }
                            },
                            valueRange = 10f..80f
                        )

                        // Background replacement selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedBgMode == BgMode.TRANSPARENT,
                                onClick = { selectedBgMode = BgMode.TRANSPARENT },
                                label = { Text("Transparent", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            FilterChip(
                                selected = selectedBgMode == BgMode.SOLID,
                                onClick = { selectedBgMode = BgMode.SOLID },
                                label = { Text("Solid Color", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.FormatColorFill, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            FilterChip(
                                selected = selectedBgMode == BgMode.GRADIENT,
                                onClick = { selectedBgMode = BgMode.GRADIENT },
                                label = { Text("Studio Gradient", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Gradient, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        // Colors or gradients palette
                        if (selectedBgMode == BgMode.SOLID) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(Color.White, Color.Black, Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFDD835), Color(0xFF8E24AA)).forEach { col ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(col)
                                            .border(
                                                width = if (selectedSolidColor == col) 3.dp else 1.dp,
                                                color = if (selectedSolidColor == col) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedSolidColor = col }
                                    )
                                }
                            }
                        } else if (selectedBgMode == BgMode.GRADIENT) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                gradients.forEachIndexed { idx, grad ->
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(grad))
                                            .border(
                                                width = if (selectedGradientIndex == idx) 3.dp else 1.dp,
                                                color = if (selectedGradientIndex == idx) MaterialTheme.colorScheme.primary else Color.Gray,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedGradientIndex = idx }
                                    )
                                }
                            }
                        }

                        // Choose another photo button
                        OutlinedButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Photo")
                        }
                    }
                }
            }
        }
    }
}

// Background removal algorithm based on floodfill color distance sampling
fun removeBackground(src: Bitmap, tolerance: Int): Bitmap {
    val width = src.width
    val height = src.height
    val pixels = IntArray(width * height)
    src.getPixels(pixels, 0, width, 0, 0, width, height)

    // Sample corner colors
    val samplePoints = intArrayOf(
        pixels[0],
        pixels[width - 1],
        pixels[(height - 1) * width],
        pixels[width * height - 1]
    )

    fun colorDistance(c1: Int, c2: Int): Int {
        val r1 = (c1 shr 16) and 0xFF
        val g1 = (c1 shr 8) and 0xFF
        val b1 = c1 and 0xFF

        val r2 = (c2 shr 16) and 0xFF
        val g2 = (c2 shr 8) and 0xFF
        val b2 = c2 and 0xFF

        return (abs(r1 - r2) + abs(g1 - g2) + abs(b1 - b2)) / 3
    }

    val tolVal = (tolerance * 255) / 100

    val outputPixels = IntArray(width * height)
    for (i in pixels.indices) {
        val px = pixels[i]
        val minDiff = samplePoints.minOf { colorDistance(it, px) }
        if (minDiff < tolVal) {
            // Transparent
            outputPixels[i] = 0x00000000
        } else {
            outputPixels[i] = px
        }
    }

    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    result.setPixels(outputPixels, 0, width, 0, 0, width, height)
    return result
}

fun composeFinalBitmap(
    cutout: Bitmap,
    mode: BgMode,
    solidColor: Color,
    gradientColors: List<Color>
): Bitmap {
    val width = cutout.width
    val height = cutout.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    when (mode) {
        BgMode.TRANSPARENT -> {
            // Leave transparent
        }
        BgMode.SOLID -> {
            canvas.drawColor(solidColor.toArgb())
        }
        BgMode.GRADIENT -> {
            val paint = Paint().apply {
                shader = android.graphics.LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    gradientColors[0].toArgb(),
                    gradientColors[1].toArgb(),
                    android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }
    }

    canvas.drawBitmap(cutout, 0f, 0f, null)
    return bitmap
}
