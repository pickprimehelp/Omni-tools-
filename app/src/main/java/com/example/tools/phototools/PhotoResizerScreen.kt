package com.example.tools.phototools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ResizePreset(
    val title: String,
    val width: Int,
    val height: Int,
    val subtitle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoResizerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resizedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var origWidth by remember { mutableIntStateOf(1080) }
    var origHeight by remember { mutableIntStateOf(1080) }

    var targetWidthStr by remember { mutableStateOf("1080") }
    var targetHeightStr by remember { mutableStateOf("1080") }
    var maintainAspect by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    val presets = listOf(
        ResizePreset("Passport Photo", 413, 531, "35x45 mm (India/Govt)"),
        ResizePreset("Instagram Square", 1080, 1080, "1:1 Feed Post"),
        ResizePreset("Instagram Story / Reel", 1080, 1920, "9:16 Fullscreen"),
        ResizePreset("YouTube Thumbnail", 1280, 720, "16:9 HD Cover"),
        ResizePreset("Facebook Post", 1200, 630, "Standard FB Feed"),
        ResizePreset("Profile Avatar", 500, 500, "Clean 1:1 Icon")
    )

    // Fallback sample
    LaunchedEffect(Unit) {
        if (originalBitmap == null) {
            val sample = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(sample)
            val paint = android.graphics.Paint()
            paint.shader = android.graphics.LinearGradient(
                0f, 0f, 1080f, 1080f,
                android.graphics.Color.rgb(16, 185, 129),
                android.graphics.Color.rgb(14, 165, 233),
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, 1080f, 1080f, paint)
            val tp = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 65f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Photo Resizer Studio", 540f, 520f, tp)
            tp.textSize = 34f
            canvas.drawText("Original: 1080 × 1080 px", 540f, 600f, tp)

            originalBitmap = sample
            resizedBitmap = sample
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(it)?.use { input ->
                        val bmp = BitmapFactory.decodeStream(input)
                        bmp?.let { loaded ->
                            withContext(Dispatchers.Main) {
                                originalBitmap = loaded
                                resizedBitmap = loaded
                                origWidth = loaded.width
                                origHeight = loaded.height
                                targetWidthStr = loaded.width.toString()
                                targetHeightStr = loaded.height.toString()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun applyResize(w: Int, h: Int) {
        val src = originalBitmap ?: return
        if (w <= 10 || h <= 10) return
        coroutineScope.launch(Dispatchers.IO) {
            isProcessing = true
            try {
                val scaled = Bitmap.createScaledBitmap(src, w, h, true)
                withContext(Dispatchers.Main) {
                    resizedBitmap = scaled
                    isProcessing = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    Toast.makeText(context, "Cannot resize: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Resizer (W × H)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            resizedBitmap?.let { bmp ->
                                AppUtils.saveAndShareBitmap(context, bmp, "Share Resized Photo")
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save & Share")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                resizedBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Resized",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                if (isProcessing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.75f))
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select Photo", fontSize = 12.sp)
                }

                // Current Dimensions Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${resizedBitmap?.width ?: 0} × ${resizedBitmap?.height ?: 0} px",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Custom Dimension Inputs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Custom Dimensions (Pixels)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = targetWidthStr,
                            onValueChange = { newW ->
                                targetWidthStr = newW
                                val w = newW.toIntOrNull()
                                if (maintainAspect && w != null && origWidth > 0) {
                                    val ratio = origHeight.toFloat() / origWidth.toFloat()
                                    targetHeightStr = (w * ratio).toInt().toString()
                                }
                            },
                            label = { Text("Width (px)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))

                        OutlinedTextField(
                            value = targetHeightStr,
                            onValueChange = { newH ->
                                targetHeightStr = newH
                                val h = newH.toIntOrNull()
                                if (maintainAspect && h != null && origHeight > 0) {
                                    val ratio = origWidth.toFloat() / origHeight.toFloat()
                                    targetWidthStr = (h * ratio).toInt().toString()
                                }
                            },
                            label = { Text("Height (px)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = maintainAspect,
                                onCheckedChange = { maintainAspect = it }
                            )
                            Text("Lock Aspect Ratio", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                val w = targetWidthStr.toIntOrNull() ?: 1080
                                val h = targetHeightStr.toIntOrNull() ?: 1080
                                applyResize(w, h)
                            }
                        ) {
                            Text("Apply Resize")
                        }
                    }
                }
            }

            // Percentage Scale Presets
            Text("Quick Scale (%)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("25%" to 0.25f, "50%" to 0.50f, "75%" to 0.75f, "150%" to 1.50f).forEach { (label, factor) ->
                    OutlinedButton(
                        onClick = {
                            val w = (origWidth * factor).toInt().coerceAtLeast(50)
                            val h = (origHeight * factor).toInt().coerceAtLeast(50)
                            targetWidthStr = w.toString()
                            targetHeightStr = h.toString()
                            applyResize(w, h)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Popular Social & Government Presets
            Text("Popular Size Presets", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            targetWidthStr = preset.width.toString()
                            targetHeightStr = preset.height.toString()
                            applyResize(preset.width, preset.height)
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(preset.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(preset.subtitle, fontSize = 11.sp, color = Color.Gray)
                            }

                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "${preset.width} × ${preset.height}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
