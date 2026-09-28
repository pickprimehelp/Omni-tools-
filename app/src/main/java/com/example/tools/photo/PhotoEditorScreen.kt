package com.example.tools.photo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PhotoFilter(val label: String) {
    NONE("Original"),
    VIVID("Vivid"),
    VINTAGE("Vintage"),
    NOIR("Noir B&W"),
    SEPIA("Sepia"),
    CYBERPUNK("Cyber Neon"),
    GOLDEN("Golden Hour")
}

data class DrawPath(val points: List<Offset>, val color: Color, val strokeWidth: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var displayedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var selectedFilter by remember { mutableStateOf(PhotoFilter.NONE) }
    var brightness by remember { mutableFloatStateOf(0f) } // -100 to 100
    var contrast by remember { mutableFloatStateOf(1f) } // 0.5 to 2.0
    var saturation by remember { mutableFloatStateOf(1f) } // 0 to 2.0
    var warmth by remember { mutableFloatStateOf(0f) } // -50 to 50

    var isBrushMode by remember { mutableStateOf(false) }
    var brushColor by remember { mutableStateOf(Color.Red) }
    var brushSize by remember { mutableFloatStateOf(10f) }
    val drawPaths = remember { mutableStateListOf<DrawPath>() }
    var currentDrawPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Filters, 1: Adjustments, 2: Draw Brush

    fun renderProcessedBitmap(src: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply { isAntiAlias = true }

        val cm = ColorMatrix()

        // 1. Filter preset base
        when (selectedFilter) {
            PhotoFilter.NONE -> {}
            PhotoFilter.VIVID -> cm.setSaturation(1.4f)
            PhotoFilter.NOIR -> cm.setSaturation(0f)
            PhotoFilter.SEPIA -> {
                cm.set(floatArrayOf(
                    0.393f, 0.769f, 0.189f, 0f, 0f,
                    0.349f, 0.686f, 0.168f, 0f, 0f,
                    0.272f, 0.534f, 0.131f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            PhotoFilter.VINTAGE -> {
                cm.set(floatArrayOf(
                    0.9f, 0.1f, 0.1f, 0f, 20f,
                    0.1f, 0.8f, 0.1f, 0f, 10f,
                    0.1f, 0.1f, 0.7f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            PhotoFilter.CYBERPUNK -> {
                cm.set(floatArrayOf(
                    1.2f, 0f, 0.2f, 0f, 30f,
                    0f, 1.1f, 0.1f, 0f, -10f,
                    0.3f, 0f, 1.5f, 0f, 40f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            PhotoFilter.GOLDEN -> {
                cm.set(floatArrayOf(
                    1.2f, 0f, 0f, 0f, 40f,
                    0f, 1.1f, 0f, 0f, 25f,
                    0f, 0f, 0.8f, 0f, -20f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
        }

        // 2. Adjustments: Contrast & Brightness
        val scale = contrast
        val translate = brightness + (1f - scale) * 128f
        val adjMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate + warmth,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate - warmth,
            0f, 0f, 0f, 1f, 0f
        ))
        cm.postConcat(adjMatrix)

        // 3. Saturation
        if (saturation != 1f) {
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(saturation)
            cm.postConcat(satMatrix)
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)

        // 4. Draw brush annotations
        val brushPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val allPaths = drawPaths.toList() + (if (currentDrawPoints.size > 1) listOf(DrawPath(currentDrawPoints, brushColor, brushSize)) else emptyList())
        for (dp in allPaths) {
            if (dp.points.size < 2) continue
            brushPaint.color = dp.color.toArgb()
            brushPaint.strokeWidth = dp.strokeWidth * (src.width.toFloat() / 600f) // scale with image
            val path = Path()
            path.moveTo(dp.points[0].x * src.width, dp.points[0].y * src.height)
            for (i in 1 until dp.points.size) {
                path.lineTo(dp.points[i].x * src.width, dp.points[i].y * src.height)
            }
            canvas.drawPath(path, brushPaint)
        }

        return result
    }

    fun refreshImage() {
        originalBitmap?.let {
            coroutineScope.launch(Dispatchers.Default) {
                val updated = renderProcessedBitmap(it)
                withContext(Dispatchers.Main) {
                    displayedBitmap = updated
                }
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(context.contentResolver, uri)
                            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
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
                    val maxDim = 1000
                    val scaled = if (bmp.width > maxDim || bmp.height > maxDim) {
                        val ratio = maxDim.toFloat() / maxOf(bmp.width, bmp.height)
                        Bitmap.createScaledBitmap(bmp, (bmp.width * ratio).toInt(), (bmp.height * ratio).toInt(), true)
                    } else bmp
                    originalBitmap = scaled
                    drawPaths.clear()
                    displayedBitmap = renderProcessedBitmap(scaled)
                }
            }
        }
    }

    LaunchedEffect(selectedFilter, brightness, contrast, saturation, warmth, drawPaths.size, currentDrawPoints.size) {
        refreshImage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pro Photo Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (displayedBitmap != null) {
                        FilledTonalButton(
                            onClick = {
                                AppUtils.saveAndShareBitmap(context, displayedBitmap!!, "Share Edited Photo")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Photo Canvas Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (displayedBitmap == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AutoFixNormal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Pro Photo Filter & Retouching", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Color grading, contrast adjustments, and drawing tools", color = Color.Gray, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { photoPicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Photo to Edit")
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(isBrushMode) {
                                if (isBrushMode) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val norm = Offset(offset.x / size.width, offset.y / size.height)
                                            currentDrawPoints = listOf(norm)
                                        },
                                        onDrag = { change, _ ->
                                            val norm = Offset(change.position.x / size.width, change.position.y / size.height)
                                            currentDrawPoints = currentDrawPoints + norm
                                        },
                                        onDragEnd = {
                                            if (currentDrawPoints.size > 1) {
                                                drawPaths.add(DrawPath(currentDrawPoints, brushColor, brushSize))
                                            }
                                            currentDrawPoints = emptyList()
                                        }
                                    )
                                }
                            }
                    ) {
                        Image(
                            bitmap = displayedBitmap!!.asImageBitmap(),
                            contentDescription = "Edited Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Brush mode active indicator
                        if (isBrushMode) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(10.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("✏️ Brush Mode Active - Draw on canvas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Controls
            if (originalBitmap != null) {
                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = {
                            activeTab = 0
                            isBrushMode = false
                        },
                        text = { Text("Filters") },
                        icon = { Icon(Icons.Default.Filter, contentDescription = null) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = {
                            activeTab = 1
                            isBrushMode = false
                        },
                        text = { Text("Tune") },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = {
                            activeTab = 2
                            isBrushMode = true
                        },
                        text = { Text("Draw") },
                        icon = { Icon(Icons.Default.Brush, contentDescription = null) }
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (activeTab) {
                            0 -> {
                                // Filters list
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    PhotoFilter.values().forEach { filter ->
                                        FilterChip(
                                            selected = selectedFilter == filter,
                                            onClick = { selectedFilter = filter },
                                            label = { Text(filter.label, fontSize = 12.sp) }
                                        )
                                    }
                                }
                            }
                            1 -> {
                                // Adjustments sliders
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Brightness", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${brightness.toInt()}", fontSize = 12.sp)
                                    }
                                    Slider(
                                        value = brightness,
                                        onValueChange = { brightness = it },
                                        valueRange = -80f..80f
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Contrast", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(String.format("%.1fx", contrast), fontSize = 12.sp)
                                    }
                                    Slider(
                                        value = contrast,
                                        onValueChange = { contrast = it },
                                        valueRange = 0.5f..2.0f
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Saturation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(String.format("%.1fx", saturation), fontSize = 12.sp)
                                    }
                                    Slider(
                                        value = saturation,
                                        onValueChange = { saturation = it },
                                        valueRange = 0f..2.0f
                                    )
                                }
                            }
                            2 -> {
                                // Drawing palette & stroke
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.White, Color.Black).forEach { col ->
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(col)
                                                    .border(
                                                        width = if (brushColor == col) 3.dp else 1.dp,
                                                        color = if (brushColor == col) MaterialTheme.colorScheme.primary else Color.Gray,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { brushColor = col }
                                            )
                                        }
                                    }

                                    if (drawPaths.isNotEmpty()) {
                                        IconButton(onClick = { drawPaths.removeLastOrNull() }) {
                                            Icon(Icons.Default.Undo, contentDescription = "Undo")
                                        }
                                    }
                                }
                            }
                        }

                        // Reset & Change Photo buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedFilter = PhotoFilter.NONE
                                    brightness = 0f
                                    contrast = 1f
                                    saturation = 1f
                                    warmth = 0f
                                    drawPaths.clear()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }
                            OutlinedButton(
                                onClick = { photoPicker.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("New Photo")
                            }
                        }
                    }
                }
            }
        }
    }
}
