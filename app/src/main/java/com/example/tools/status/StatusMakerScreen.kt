package com.example.tools.status

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.AppUtils

data class QuoteItem(val text: String, val author: String, val category: String)

val STATUS_QUOTES = listOf(
    QuoteItem("Dream big, work hard, stay focused and surround yourself with good people.", "Success Mindset", "Motivation"),
    QuoteItem("Never stop learning because life never stops teaching.", "Daily Wisdom", "Motivation"),
    QuoteItem("मंज़िल उन्हीं को मिलती है, जिनके सपनों में जान होती है!", "हिंदी सुविचार", "Hindi"),
    QuoteItem("वक्त से लड़कर जो नसीब बदल दे, इंसान वही जो अपनी तकदीर बदल दे!", "जोश शायरी", "Hindi"),
    QuoteItem("In your smile, I find a peaceful world where only happiness exists.", "Romantic Soul", "Love"),
    QuoteItem("Every moment spent with you is like a beautiful dream come true.", "Sweet Heart", "Love"),
    QuoteItem("Silent wolves hunt in the dark. Don't mistake patience for surrender.", "King Mindset", "Attitude"),
    QuoteItem("I am not a second option, either you choose me or lose me.", "Bold Life", "Attitude"),
    QuoteItem("Rise and shine! Today is a brand new page in your journey.", "Fresh Start", "Morning"),
    QuoteItem("May the stars light your dreams and silence heal your soul tonight.", "Peaceful Night", "Night")
)

val STATUS_GRADIENTS = listOf(
    listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)),
    listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
    listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
    listOf(Color(0xFFFF512F), Color(0xFFDD2476)),
    listOf(Color(0xFF121212), Color(0xFF282828)),
    listOf(Color(0xFF654EA3), Color(0xFFEAAFC8))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusMakerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var quoteText by remember { mutableStateOf(STATUS_QUOTES[0].text) }
    var authorText by remember { mutableStateOf(STATUS_QUOTES[0].author) }
    var selectedCategory by remember { mutableStateOf("Motivation") }
    var selectedGradientIndex by remember { mutableIntStateOf(0) }
    var customImageUri by remember { mutableStateOf<Uri?>(null) }
    var fontStyle by remember { mutableStateOf("Bold") } // Bold, Serif, Modern
    var stickerText by remember { mutableStateOf("🔥") }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Story & Status Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            val bitmap = generateStatusBitmap(
                                quote = quoteText,
                                author = authorText,
                                sticker = stickerText,
                                gradient = STATUS_GRADIENTS[selectedGradientIndex]
                            )
                            AppUtils.saveAndShareBitmap(context, bitmap, "Share Story Status")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share 9:16")
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
            // Live 9:16 Story Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.68f)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(STATUS_GRADIENTS[selectedGradientIndex])),
                    contentAlignment = Alignment.Center
                ) {
                    if (customImageUri != null) {
                        AsyncImage(
                            model = customImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Sticker & Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stickerText, fontSize = 28.sp)
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("STORY STATUS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            }
                        }

                        // Center Quote
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 60.sp,
                                lineHeight = 30.sp
                            )
                            Text(
                                text = quoteText,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = if (fontStyle == "Bold") FontWeight.Bold else FontWeight.Medium,
                                fontFamily = if (fontStyle == "Serif") FontFamily.Serif else FontFamily.SansSerif,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "— $authorText",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Bottom Branding
                        Text(
                            text = "#OmniToolStudio",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            // Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Motivation", "Hindi", "Love", "Attitude", "Morning", "Night").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            val match = STATUS_QUOTES.firstOrNull { it.category == cat }
                            if (match != null) {
                                quoteText = match.text
                                authorText = match.author
                            }
                        },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            // Quick Quote Picker
            Text("Trending Quotes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                STATUS_QUOTES.filter { it.category == selectedCategory }.forEach { q ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable {
                                quoteText = q.text
                                authorText = q.author
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(q.text, maxLines = 3, fontSize = 12.sp, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(q.author, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Background & Stickers
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Themes & Backgrounds", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        STATUS_GRADIENTS.forEachIndexed { idx, grad ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(grad))
                                    .border(
                                        width = if (selectedGradientIndex == idx && customImageUri == null) 3.dp else 1.dp,
                                        color = if (selectedGradientIndex == idx && customImageUri == null) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedGradientIndex = idx
                                        customImageUri = null
                                    }
                            )
                        }
                    }

                    // Stickers
                    Text("Vibe Stickers", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("🔥", "✨", "❤️", "👑", "🚀", "🌸", "☕", "🌙").forEach { stk ->
                            FilterChip(
                                selected = stickerText == stk,
                                onClick = { stickerText = stk },
                                label = { Text(stk, fontSize = 18.sp) }
                            )
                        }
                    }

                    // Custom Photo Background
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { photoPicker.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (customImageUri == null) "Photo Backdrop" else "Change Photo")
                        }
                        if (customImageUri != null) {
                            OutlinedButton(onClick = { customImageUri = null }) {
                                Text("Clear")
                            }
                        }
                    }

                    // Custom Text inputs
                    OutlinedTextField(
                        value = quoteText,
                        onValueChange = { quoteText = it },
                        label = { Text("Status / Quote Text") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = authorText,
                        onValueChange = { authorText = it },
                        label = { Text("Author / Signature") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

fun generateStatusBitmap(
    quote: String,
    author: String,
    sticker: String,
    gradient: List<Color>
): Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background gradient
    val paint = Paint().apply {
        isAntiAlias = true
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            gradient[0].hashCode(),
            gradient[1].hashCode(),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    // Sticker
    val stickerPaint = Paint().apply {
        textSize = 100f
        isAntiAlias = true
    }
    canvas.drawText(sticker, 100f, 220f, stickerPaint)

    // Quote
    val quotePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 58f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    // Wrap quote text into lines
    val words = quote.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""
    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (quotePaint.measureText(testLine) > 850f) {
            lines.add(currentLine)
            currentLine = word
        } else {
            currentLine = testLine
        }
    }
    if (currentLine.isNotEmpty()) lines.add(currentLine)

    var startY = (height / 2f) - (lines.size * 35f)
    for (line in lines) {
        canvas.drawText(line, width / 2f, startY, quotePaint)
        startY += 80f
    }

    // Author
    val authorPaint = Paint().apply {
        color = android.graphics.Color.argb(220, 255, 255, 255)
        textAlign = Paint.Align.CENTER
        textSize = 38f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    canvas.drawText("— $author", width / 2f, startY + 40f, authorPaint)

    // Footer
    val footerPaint = Paint().apply {
        color = android.graphics.Color.argb(130, 255, 255, 255)
        textAlign = Paint.Align.CENTER
        textSize = 28f
        isAntiAlias = true
    }
    canvas.drawText("#OmniToolStudio", width / 2f, height - 120f, footerPaint)

    return bitmap
}
