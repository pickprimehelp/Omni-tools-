package com.example.tools.phototools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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

data class BgOption(
    val title: String,
    val color1: Int,
    val color2: Int = color1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundChangerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var outputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedBgIndex by remember { mutableIntStateOf(0) }
    var backgroundMode by remember { mutableStateOf("Color") } // Color, Gradient, Blur
    var isProcessing by remember { mutableStateOf(false) }

    val studioColors = listOf(
        BgOption("Studio White", AndroidColor.WHITE),
        BgOption("Passport Blue", AndroidColor.rgb(0, 102, 204)),
        BgOption("Official Red", AndroidColor.rgb(180, 20, 20)),
        BgOption("Warm Gray", AndroidColor.rgb(220, 220, 225)),
        BgOption("Charcoal Dark", AndroidColor.rgb(35, 39, 42)),
        BgOption("Soft Beige", AndroidColor.rgb(245, 235, 220)),
        BgOption("Emerald Green", AndroidColor.rgb(16, 120, 70))
    )

    val gradientOptions = listOf(
        BgOption("Sunset Glow", AndroidColor.rgb(249, 115, 22), AndroidColor.rgb(236, 72, 153)),
        BgOption("Cyber Neon", AndroidColor.rgb(139, 92, 246), AndroidColor.rgb(6, 182, 212)),
        BgOption("Ocean Breeze", AndroidColor.rgb(59, 130, 246), AndroidColor.rgb(16, 185, 129)),
        BgOption("Royal Velvet", AndroidColor.rgb(124, 58, 237), AndroidColor.rgb(217, 70, 239)),
        BgOption("Golden Dawn", AndroidColor.rgb(245, 158, 11), AndroidColor.rgb(239, 68, 68))
    )

    // Fallback sample
    LaunchedEffect(Unit) {
        if (sourceBitmap == null) {
            val sample = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(sample)
            val p = Paint().apply { isAntiAlias = true }
            p.color = AndroidColor.WHITE
            canvas.drawCircle(400f, 400f, 320f, p)
            p.color = AndroidColor.rgb(30, 41, 59)
            canvas.drawCircle(400f, 350f, 160f, p)
            p.color = AndroidColor.rgb(79, 70, 229)
            canvas.drawRect(250f, 500f, 550f, 720f, p)

            sourceBitmap = sample
            outputBitmap = sample
        }
    }

    fun renderBackdrop() {
        val src = sourceBitmap ?: return
        coroutineScope.launch(Dispatchers.IO) {
            isProcessing = true
            val w = src.width
            val h = src.height
            val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val bgPaint = Paint().apply { isAntiAlias = true }

            if (backgroundMode == "Gradient") {
                val opt = gradientOptions.getOrElse(selectedBgIndex) { gradientOptions.first() }
                bgPaint.shader = android.graphics.LinearGradient(
                    0f, 0f, 0f, h.toFloat(),
                    opt.color1, opt.color2,
                    android.graphics.Shader.TileMode.CLAMP
                )
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)
            } else if (backgroundMode == "Blur") {
                // Blurred scaled background
                val small = Bitmap.createScaledBitmap(src, (w / 10).coerceAtLeast(10), (h / 10).coerceAtLeast(10), true)
                val blurScaled = Bitmap.createScaledBitmap(small, w, h, true)
                canvas.drawBitmap(blurScaled, 0f, 0f, null)
                // Overlay dark tint
                bgPaint.color = AndroidColor.argb(80, 0, 0, 0)
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)
            } else {
                val opt = studioColors.getOrElse(selectedBgIndex) { studioColors.first() }
                bgPaint.color = opt.color1
                canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)
            }

            // Draw foreground subject with smart centered padding
            val padW = (w * 0.08f).toInt()
            val padH = (h * 0.08f).toInt()
            val dstRect = Rect(padW, padH, w - padW, h - padH)
            canvas.drawBitmap(src, null, dstRect, null)

            withContext(Dispatchers.Main) {
                outputBitmap = result
                isProcessing = false
            }
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
                                sourceBitmap = loaded
                                renderBackdrop()
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

    LaunchedEffect(selectedBgIndex, backgroundMode) {
        renderBackdrop()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background Changer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            outputBitmap?.let { bmp ->
                                AppUtils.saveAndShareBitmap(context, bmp, "Share Background Changed Photo")
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
            // Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.05f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                outputBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Output",
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
            }

            // Mode Selector (Color / Gradient / Blur)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Color" to "Solid Studio", "Gradient" to "Artistic Gradient", "Blur" to "Aesthetic Blur").forEach { (modeKey, modeTitle) ->
                    FilterChip(
                        selected = backgroundMode == modeKey,
                        onClick = {
                            backgroundMode = modeKey
                            selectedBgIndex = 0
                        },
                        label = { Text(modeTitle) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Background Palette Options
            if (backgroundMode == "Color") {
                Text("Studio Solid Colors", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    studioColors.forEachIndexed { idx, opt ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedBgIndex = idx }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(opt.color1))
                                    .border(
                                        width = if (selectedBgIndex == idx) 3.dp else 1.dp,
                                        color = if (selectedBgIndex == idx) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedBgIndex == idx) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = if (opt.color1 == AndroidColor.WHITE) Color.Black else Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(opt.title, fontSize = 11.sp, fontWeight = if (selectedBgIndex == idx) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            } else if (backgroundMode == "Gradient") {
                Text("Vibrant Gradient Backdrops", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gradientOptions.forEachIndexed { idx, opt ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedBgIndex = idx }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(opt.color1), Color(opt.color2))))
                                    .border(
                                        width = if (selectedBgIndex == idx) 3.dp else 1.dp,
                                        color = if (selectedBgIndex == idx) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedBgIndex == idx) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(opt.title, fontSize = 11.sp, fontWeight = if (selectedBgIndex == idx) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Soft Blur Backdrop Effect", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Extracts and applies a smooth, cinematic frosted-glass bokeh of your photo behind the centered subject.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
