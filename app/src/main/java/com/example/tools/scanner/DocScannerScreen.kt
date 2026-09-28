package com.example.tools.scanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScanFilter(val label: String) {
    ORIGINAL("Original"),
    MAGIC_COLOR("Magic Color"),
    CRISP_BW("B&W Document"),
    GRAYSCALE("Grayscale")
}

data class ScannedPage(
    val id: String,
    val original: Bitmap,
    var filtered: Bitmap,
    var filter: ScanFilter = ScanFilter.MAGIC_COLOR
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val pages = remember { mutableStateListOf<ScannedPage>() }
    var selectedPageIndex by remember { mutableIntStateOf(0) }
    var ocrText by remember { mutableStateOf("") }
    var isOcrRunning by remember { mutableStateOf(false) }
    var showOcrDialog by remember { mutableStateOf(false) }

    fun applyFilterToBitmap(src: Bitmap, filter: ScanFilter): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply { isAntiAlias = true }

        val cm = ColorMatrix()
        when (filter) {
            ScanFilter.ORIGINAL -> {
                // Identity
            }
            ScanFilter.MAGIC_COLOR -> {
                // High contrast vibrant document
                cm.set(floatArrayOf(
                    1.3f, 0f, 0f, 0f, -25f,
                    0f, 1.3f, 0f, 0f, -25f,
                    0f, 0f, 1.3f, 0f, -25f,
                    0f, 0f, 0f, 1f, 0f
                ))
            }
            ScanFilter.CRISP_BW -> {
                cm.setSaturation(0f)
                val bwMatrix = ColorMatrix(floatArrayOf(
                    2.5f, 0f, 0f, 0f, -180f,
                    0f, 2.5f, 0f, 0f, -180f,
                    0f, 0f, 2.5f, 0f, -180f,
                    0f, 0f, 0f, 1f, 0f
                ))
                cm.postConcat(bwMatrix)
            }
            ScanFilter.GRAYSCALE -> {
                cm.setSaturation(0f)
            }
        }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                uris.forEach { uri ->
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
                        // Max dimension 1200 for clean scanning & memory efficiency
                        val maxDim = 1200
                        val scaled = if (bmp.width > maxDim || bmp.height > maxDim) {
                            val ratio = maxDim.toFloat() / maxOf(bmp.width, bmp.height)
                            Bitmap.createScaledBitmap(bmp, (bmp.width * ratio).toInt(), (bmp.height * ratio).toInt(), true)
                        } else bmp
                        val filtered = applyFilterToBitmap(scaled, ScanFilter.MAGIC_COLOR)
                        pages.add(ScannedPage(System.currentTimeMillis().toString(), scaled, filtered))
                    }
                }
                if (selectedPageIndex >= pages.size && pages.isNotEmpty()) {
                    selectedPageIndex = pages.size - 1
                }
            }
        }
    }

    fun runOcrOnCurrentPage() {
        if (pages.isEmpty()) return
        val currentPage = pages[selectedPageIndex]
        isOcrRunning = true
        showOcrDialog = true
        ocrText = "Scanning text using ML OCR..."

        try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromBitmap(currentPage.filtered, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    ocrText = if (visionText.text.isNotBlank()) visionText.text else "No text detected in this page."
                    isOcrRunning = false
                }
                .addOnFailureListener { e ->
                    ocrText = "OCR failed: ${e.localizedMessage}"
                    isOcrRunning = false
                }
        } catch (e: Exception) {
            ocrText = "OCR Error: ${e.localizedMessage}"
            isOcrRunning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doc Scanner & OCR", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (pages.isNotEmpty()) {
                        IconButton(onClick = { runOcrOnCurrentPage() }) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = "OCR Text")
                        }
                        FilledTonalButton(
                            onClick = {
                                val pdfFile = AppUtils.createPdfFromPages(context, pages.map { it.filtered })
                                AppUtils.sharePdf(context, pdfFile, "Share Scanned PDF")
                            }
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export PDF")
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
            // Main document viewer area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (pages.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("HD Document Scanner & OCR", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Scan documents, enhance contrast, extract text with AI OCR & export clean multi-page PDFs.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { docPickerLauncher.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Document / Photos")
                        }
                    }
                } else {
                    val activePage = pages[selectedPageIndex]
                    Image(
                        bitmap = activePage.filtered.asImageBitmap(),
                        contentDescription = "Document Page",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )

                    // Page indicator badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Page ${selectedPageIndex + 1} of ${pages.size}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Delete current page button
                    IconButton(
                        onClick = {
                            pages.removeAt(selectedPageIndex)
                            if (selectedPageIndex >= pages.size && pages.isNotEmpty()) {
                                selectedPageIndex = pages.size - 1
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Page", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Controls when pages exist
            if (pages.isNotEmpty()) {
                val activePage = pages[selectedPageIndex]

                // Scanned pages thumbnail strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pages (${pages.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    FilledTonalButton(
                        onClick = { docPickerLauncher.launch("image/*") },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Pages", fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pages.forEachIndexed { idx, page ->
                        Box(
                            modifier = Modifier
                                .width(65.dp)
                                .height(85.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (selectedPageIndex == idx) 3.dp else 1.dp,
                                    color = if (selectedPageIndex == idx) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedPageIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = page.filtered.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "${idx + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Filter options
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
                        Text("Scanner Filter", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ScanFilter.values().forEach { filter ->
                                FilterChip(
                                    selected = activePage.filter == filter,
                                    onClick = {
                                        activePage.filter = filter
                                        activePage.filtered = applyFilterToBitmap(activePage.original, filter)
                                    },
                                    label = { Text(filter.label, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // OCR Result Dialog
    if (showOcrDialog) {
        AlertDialog(
            onDismissRequest = { showOcrDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extracted OCR Text")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isOcrRunning) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Recognizing text from scan...")
                        }
                    } else {
                        SelectionContainer {
                            Text(ocrText, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AppUtils.copyToClipboard(context, ocrText, "OCR Scanned Text")
                    },
                    enabled = !isOcrRunning && ocrText.isNotBlank()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Text")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOcrDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SelectionContainer(content: @Composable () -> Unit) {
    androidx.compose.foundation.text.selection.SelectionContainer {
        content()
    }
}
