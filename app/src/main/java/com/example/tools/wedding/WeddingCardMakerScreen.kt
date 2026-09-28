package com.example.tools.wedding

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

enum class WeddingTemplate(val title: String, val bgColors: List<Color>, val accentColor: Color, val textColor: Color) {
    ROYAL_GOLD("Royal Ruby", listOf(Color(0xFF4A0E17), Color(0xFF2A080C)), Color(0xFFFFD700), Color(0xFFFFF8DC)),
    PASTEL_ROSE("Blush Floral", listOf(Color(0xFFFFF0F5), Color(0xFFFFE4E1)), Color(0xFFC71585), Color(0xFF333333)),
    EMERALD_LUXURY("Emerald Regalia", listOf(Color(0xFF0F382A), Color(0xFF051C14)), Color(0xFFE2C974), Color(0xFFF0FFF0)),
    MIDNIGHT_ROYAL("Sapphire Palace", listOf(Color(0xFF141E30), Color(0xFF243B55)), Color(0xFFFFCC00), Color(0xFFFFFFFF))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeddingCardMakerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var groomName by remember { mutableStateOf("Rahul Sharma") }
    var brideName by remember { mutableStateOf("Pooja Verma") }
    var eventType by remember { mutableStateOf("Wedding Ceremony") }
    var eventDate by remember { mutableStateOf("Sunday, December 12, 2026") }
    var eventTime by remember { mutableStateOf("7:00 PM Onwards") }
    var venue by remember { mutableStateOf("The Grand Heritage Palace, Royal Hall, New Delhi") }
    var rsvpText by remember { mutableStateOf("RSVP: Sharma & Verma Families • +91 98765 43210") }
    var selectedTemplate by remember { mutableStateOf(WeddingTemplate.ROYAL_GOLD) }
    var coupleImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSymbol by remember { mutableStateOf("🕉️") }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Details, 1: Styles, 2: Photos

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coupleImageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wedding Card Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            val cardBitmap = generateWeddingCardBitmap(
                                groomName = groomName,
                                brideName = brideName,
                                eventType = eventType,
                                date = eventDate,
                                time = eventTime,
                                venue = venue,
                                rsvp = rsvpText,
                                template = selectedTemplate,
                                symbol = selectedSymbol
                            )
                            AppUtils.saveAndShareBitmap(context, cardBitmap, "Share Wedding Invitation")
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Card")
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
            // Live Card Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .shadow(12.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(selectedTemplate.bgColors))
                        .border(3.dp, selectedTemplate.accentColor, RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner ornate border
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, selectedTemplate.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedSymbol,
                                fontSize = 34.sp
                            )

                            Text(
                                text = "TOGETHER WITH OUR FAMILIES",
                                color = selectedTemplate.accentColor,
                                fontSize = 11.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "INVITE YOU TO CELEBRATE THE",
                                color = selectedTemplate.textColor.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = eventType.uppercase(),
                                color = selectedTemplate.accentColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )

                            // Couple Photo if added
                            if (coupleImageUri != null) {
                                AsyncImage(
                                    model = coupleImageUri,
                                    contentDescription = "Couple Photo",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, selectedTemplate.accentColor, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Couple Names
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = groomName,
                                    color = selectedTemplate.textColor,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "&",
                                    color = selectedTemplate.accentColor,
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Cursive
                                )
                                Text(
                                    text = brideName,
                                    color = selectedTemplate.textColor,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Date & Time
                            Card(
                                colors = CardDefaults.cardColors(containerColor = selectedTemplate.accentColor.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = eventDate,
                                        color = selectedTemplate.accentColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = eventTime,
                                        color = selectedTemplate.textColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Venue
                            Text(
                                text = venue,
                                color = selectedTemplate.textColor.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )

                            // RSVP
                            Text(
                                text = rsvpText,
                                color = selectedTemplate.accentColor,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Tab navigation
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Details") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Themes") },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Photo & Icons") },
                    icon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null) }
                )
            }

            // Tab Content
            when (activeTab) {
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = groomName,
                                onValueChange = { groomName = it },
                                label = { Text("Groom Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = brideName,
                                onValueChange = { brideName = it },
                                label = { Text("Bride Name") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = eventType,
                            onValueChange = { eventType = it },
                            label = { Text("Event Type (e.g. Wedding, Reception)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = eventDate,
                                onValueChange = { eventDate = it },
                                label = { Text("Date") },
                                modifier = Modifier.weight(1.3f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = eventTime,
                                onValueChange = { eventTime = it },
                                label = { Text("Time") },
                                modifier = Modifier.weight(0.9f),
                                singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = venue,
                            onValueChange = { venue = it },
                            label = { Text("Venue & City") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                        OutlinedTextField(
                            value = rsvpText,
                            onValueChange = { rsvpText = it },
                            label = { Text("RSVP / Contacts") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Card Theme Template", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            WeddingTemplate.values().forEach { template ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clickable { selectedTemplate = template }
                                        .border(
                                            width = if (selectedTemplate == template) 3.dp else 1.dp,
                                            color = if (selectedTemplate == template) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.verticalGradient(template.bgColors))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = template.title,
                                            color = template.accentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Auspicious Icons & Symbols", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf("🕉️", "💍", "🪔", "💐", "✨", "🌺", "❤️", "🥂").forEach { sym ->
                                OutlinedButton(
                                    onClick = { selectedSymbol = sym },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedSymbol == sym) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                ) {
                                    Text(sym, fontSize = 24.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Couple Photo", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (coupleImageUri == null) "Select Photo" else "Change Photo")
                            }
                            if (coupleImageUri != null) {
                                OutlinedButton(
                                    onClick = { coupleImageUri = null },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Remove")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Generates high-res 1080x1500 printable wedding card
fun generateWeddingCardBitmap(
    groomName: String,
    brideName: String,
    eventType: String,
    date: String,
    time: String,
    venue: String,
    rsvp: String,
    template: WeddingTemplate,
    symbol: String
): Bitmap {
    val width = 1080
    val height = 1520
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply {
        isAntiAlias = true
        shader = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            template.bgColors[0].hashCode(),
            template.bgColors[1].hashCode(),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Outer border
    val borderPaint = Paint().apply {
        color = template.accentColor.hashCode()
        style = Paint.Style.STROKE
        strokeWidth = 14f
        isAntiAlias = true
    }
    canvas.drawRoundRect(30f, 30f, width - 30f, height - 30f, 36f, 36f, borderPaint)

    // Inner subtle border
    val innerBorder = Paint().apply {
        color = template.accentColor.hashCode()
        style = Paint.Style.STROKE
        strokeWidth = 3f
        alpha = 140
        isAntiAlias = true
    }
    canvas.drawRoundRect(60f, 60f, width - 60f, height - 60f, 24f, 24f, innerBorder)

    // Text paints
    val textPaint = Paint().apply {
        color = template.textColor.hashCode()
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    val accentPaint = Paint().apply {
        color = template.accentColor.hashCode()
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // Symbol
    val symbolPaint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 90f
        isAntiAlias = true
    }
    canvas.drawText(symbol, width / 2f, 180f, symbolPaint)

    accentPaint.textSize = 34f
    canvas.drawText("TOGETHER WITH OUR FAMILIES", width / 2f, 270f, accentPaint)

    textPaint.textSize = 28f
    canvas.drawText("CORDIALLY INVITE YOU TO CELEBRATE THE", width / 2f, 330f, textPaint)

    accentPaint.textSize = 46f
    canvas.drawText(eventType.uppercase(), width / 2f, 410f, accentPaint)

    // Couple Names
    textPaint.textSize = 72f
    canvas.drawText(groomName, width / 2f, 580f, textPaint)

    accentPaint.textSize = 64f
    canvas.drawText("&", width / 2f, 670f, accentPaint)

    textPaint.textSize = 72f
    canvas.drawText(brideName, width / 2f, 760f, textPaint)

    // Date & Time
    accentPaint.textSize = 42f
    canvas.drawText(date, width / 2f, 930f, accentPaint)

    textPaint.textSize = 36f
    canvas.drawText(time, width / 2f, 990f, textPaint)

    // Venue
    val venueWords = venue.split(",")
    var currentY = 1120f
    venueWords.forEach { line ->
        textPaint.textSize = 32f
        canvas.drawText(line.trim(), width / 2f, currentY, textPaint)
        currentY += 50f
    }

    // RSVP
    accentPaint.textSize = 28f
    canvas.drawText(rsvp, width / 2f, 1420f, accentPaint)

    return bitmap
}
