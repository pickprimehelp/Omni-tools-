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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoCompressorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalSizeBytes by remember { mutableLongStateOf(0L) }
    var compressedSizeBytes by remember { mutableLongStateOf(0L) }
    var quality by remember { mutableFloatStateOf(60f) }
    var isCompressing by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("JPEG") } // JPEG, WEBP

    // Sample fallback image if none selected yet
    LaunchedEffect(Unit) {
        if (originalBitmap == null) {
            val sample = Bitmap.createBitmap(1200, 1600, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(sample)
            val paint = android.graphics.Paint()
            paint.shader = android.graphics.LinearGradient(
                0f, 0f, 1200f, 1600f,
                android.graphics.Color.rgb(59, 130, 246),
                android.graphics.Color.rgb(147, 51, 234),
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, 1200f, 1600f, paint)

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 70f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("OmniTool Compressor", 600f, 750f, textPaint)
            textPaint.textSize = 40f
            canvas.drawText("Tap 'Choose Photo' below to compress", 600f, 850f, textPaint)

            originalBitmap = sample
            val bos = ByteArrayOutputStream()
            sample.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            originalSizeBytes = bos.toByteArray().size.toLong()
            compressedSizeBytes = originalSizeBytes
            compressedBitmap = sample
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(it)?.use { input ->
                        val bytes = input.readBytes()
                        originalSizeBytes = bytes.size.toLong()
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        withContext(Dispatchers.Main) {
                            originalBitmap = bmp
                            compressedBitmap = bmp
                            compressedSizeBytes = originalSizeBytes
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun compressImage(q: Float) {
        val src = originalBitmap ?: return
        coroutineScope.launch(Dispatchers.IO) {
            isCompressing = true
            val bos = ByteArrayOutputStream()
            val format = if (selectedFormat == "WEBP") {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            } else {
                Bitmap.CompressFormat.JPEG
            }
            src.compress(format, q.toInt().coerceIn(1, 100), bos)
            val compressedBytes = bos.toByteArray()
            val resultBmp = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)

            withContext(Dispatchers.Main) {
                compressedSizeBytes = compressedBytes.size.toLong()
                compressedBitmap = resultBmp
                isCompressing = false
            }
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val df = DecimalFormat("#.##")
        return if (bytes >= 1024 * 1024) {
            "${df.format(bytes / (1024.0 * 1024.0))} MB"
        } else {
            "${df.format(bytes / 1024.0)} KB"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo Compressor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            compressedBitmap?.let { bmp ->
                                AppUtils.saveAndShareBitmap(context, bmp, "Share Compressed Photo")
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
            // Hero Size Reduction Card
            val reductionPercent = if (originalSizeBytes > 0 && compressedSizeBytes > 0) {
                ((originalSizeBytes - compressedSizeBytes).toFloat() / originalSizeBytes * 100).coerceAtLeast(0f)
            } else 0f

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Original Size", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text(formatFileSize(originalSizeBytes), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "-${DecimalFormat("#.#").format(reductionPercent)}%",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Compressed Size", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text(formatFileSize(compressedSizeBytes), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                    }
                }
            }

            // Image Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                compressedBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                if (isCompressing) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                // Pick image button overlay
                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.75f))
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choose Photo", fontSize = 12.sp)
                }
            }

            // Target Quality Slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Compression Quality", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${quality.toInt()}%", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Slider(
                        value = quality,
                        onValueChange = {
                            quality = it
                        },
                        onValueChangeFinished = {
                            compressImage(quality)
                        },
                        valueRange = 5f..95f,
                        steps = 17
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Smaller File (High Compression)", fontSize = 11.sp, color = Color.Gray)
                        Text("Better Quality", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            // Quick Target Presets
            Text("Quick Presets (Target Sizes)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Low (30%)" to 30f,
                    "Medium (50%)" to 50f,
                    "High (75%)" to 75f,
                    "Web (20%)" to 20f
                ).forEach { (label, qVal) ->
                    OutlinedButton(
                        onClick = {
                            quality = qVal
                            compressImage(qVal)
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(label, fontSize = 11.sp)
                    }
                }
            }

            // Format Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Output Format:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                FilterChip(
                    selected = selectedFormat == "JPEG",
                    onClick = {
                        selectedFormat = "JPEG"
                        compressImage(quality)
                    },
                    label = { Text("JPG / JPEG") }
                )
                FilterChip(
                    selected = selectedFormat == "WEBP",
                    onClick = {
                        selectedFormat = "WEBP"
                        compressImage(quality)
                    },
                    label = { Text("Modern WEBP") }
                )
            }
        }
    }
}
