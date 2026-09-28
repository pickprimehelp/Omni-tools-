package com.example.tools.textdesign

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUtils

data class FontTransform(val name: String, val transform: (String) -> String)

val FONT_STYLES = listOf(
    FontTransform("Royal Script 𝓕𝓪𝓷𝓬𝔂") { text ->
        val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val fancy = "𝓪𝓫𝓬𝓭𝓮𝓯𝓰𝓱𝓲𝓳𝓴𝓵𝓶𝓷𝓸𝓹𝓺𝓻𝓼𝓽𝓾𝓿𝔀𝔁𝔂𝔃𝓐𝓑𝓒𝓓𝓔𝓕𝓖𝓗𝓘𝓙𝓚𝓛𝓜𝓝𝓞𝓟𝓠𝓡𝓢𝓣𝓤𝓥𝓦𝓧𝓨𝓩"
        text.map { c ->
            val idx = normal.indexOf(c)
            if (idx != -1 && idx * 2 < fancy.length) fancy.substring(idx * 2, idx * 2 + 2) else c.toString()
        }.joinToString("")
    },
    FontTransform("Bold Gothic 𝕲𝖔𝖙𝖍𝖎𝖈") { text ->
        val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val gothic = "𝖆𝖇𝖈𝖉𝖊𝖋𝖌𝖍𝖎𝖏𝖐𝖑𝖒𝖓𝖔𝖕𝖖𝖗𝖘𝖙𝖚𝖛𝖜𝖝𝖞𝖟𝕬𝕭𝕮𝕯𝕰𝕱𝕲𝕳𝕴𝕵𝕶𝕷𝕸𝕹𝕺𝕻𝕼𝕽𝕾𝕿𝖀𝖁𝖂𝖃𝖄𝖅"
        text.map { c ->
            val idx = normal.indexOf(c)
            if (idx != -1 && idx * 2 < gothic.length) gothic.substring(idx * 2, idx * 2 + 2) else c.toString()
        }.joinToString("")
    },
    FontTransform("Double Struck 𝔻𝕠𝕦𝕓𝕝𝕖") { text ->
        val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val dbl = "𝕒𝕓𝕔𝕕𝕖𝕗𝕘𝕙𝕚𝕛𝕜𝕝𝕞𝕟𝕠𝕡𝕢𝕣𝕤𝕥𝕦𝕧𝕨𝕩𝕪𝕫𝔸𝔹ℂ𝔻𝔼𝔽𝔾ℍ𝕀𝕁𝕂𝕃𝕄ℕ𝕆ℙℚℝ𝕊𝕋𝕌𝕍𝕎𝕏𝕐ℤ"
        text.map { c ->
            val idx = normal.indexOf(c)
            if (idx != -1 && idx * 2 < dbl.length) dbl.substring(idx * 2, idx * 2 + 2) else c.toString()
        }.joinToString("")
    },
    FontTransform("Bubble Circle 🅑🅤🅑🅑🅛🅔") { text ->
        val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val bubble = "🅐🅑🅒🅓🅔🅕🅖🅗🅘🅙🅚🅛🅜🅝🅞🅟🅠🅡🅢🅣🅤🅥🅦🅧🅨🅩🅐🅑🅒🅓🅔🅕🅖🅗🅘🅙🅚🅛🅜🅝🅞🅟🅠🅡🅢🅣🅤🅥🅦🅧🅨🅩"
        text.map { c ->
            val idx = normal.indexOf(c)
            if (idx != -1 && idx * 2 < bubble.length) bubble.substring(idx * 2, idx * 2 + 2) else c.toString()
        }.joinToString("")
    },
    FontTransform("Square Box 🆂🆀🆄🅰🆁🅴") { text ->
        val normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val sq = "🅰🅱🅲🅳🅴🅵🅶🅷🅸🅹🅺🅻🅼🅽🅾🅿🆀🆁🆂🆃🆄🆅🆆🆇🆈🆉🅰🅱🅲🅳🅴🅵🅶🅷🅸🅹🅺🅻🅼🅽🅾🅿🆀🆁🆂🆃🆄🆅🆆🆇🆈🆉"
        text.map { c ->
            val idx = normal.indexOf(c)
            if (idx != -1 && idx * 2 < sq.length) sq.substring(idx * 2, idx * 2 + 2) else c.toString()
        }.joinToString("")
    },
    FontTransform("Royal Border ꧁༺ Text ༻꧂") { text -> "꧁༺ $text ༻꧂" },
    FontTransform("Star Sparkle ★ Text ★") { text -> "★彡 $text 彡★" },
    FontTransform("Heart Wings ༺♥ Text ♥༻") { text -> "༺♥ $text ♥༻" },
    FontTransform("Strike Through S̶t̶r̶i̶k̶e̶") { text -> text.map { "$it\u0336" }.joinToString("") },
    FontTransform("Wide Spaced W i d e") { text -> text.toCharArray().joinToString("  ") }
)

val WORD_GRADIENTS = listOf(
    listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)),
    listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
    listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
    listOf(Color(0xFFFF512F), Color(0xFFDD2476)),
    listOf(Color(0xFF141E30), Color(0xFF243B55))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDesignScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("OmniTool Studio") }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Fancy Fonts, 1: Colorful Word Art
    var selectedGradientIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fancy Text & Color Art", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("Enter Your Text / Name / Bio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { inputText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Fancy Fonts (${FONT_STYLES.size})") },
                    icon = { Icon(Icons.Default.FormatSize, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Color Word Art") },
                    icon = { Icon(Icons.Default.ColorLens, contentDescription = null) }
                )
            }

            when (activeTab) {
                0 -> {
                    // List of generated fonts
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(FONT_STYLES) { item ->
                            val transformed = item.transform(if (inputText.isNotBlank()) inputText else "OmniTool")

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(transformed, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = { AppUtils.copyToClipboard(context, transformed, item.name) }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                        }
                                        IconButton(onClick = { AppUtils.shareText(context, transformed, "Share Fancy Text") }) {
                                            Icon(Icons.Default.Share, contentDescription = "Share")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Colorful Word Art Card Generator
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Word art preview card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(WORD_GRADIENTS[selectedGradientIndex]))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (inputText.isNotBlank()) inputText else "YOUR TEXT HERE",
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        lineHeight = 34.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "★ DESIGNED WITH OMNITOOL ★",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }

                        // Gradient palette
                        Text("Select Color Palette", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WORD_GRADIENTS.forEachIndexed { idx, grad ->
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
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

                        // Share / Export badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val bitmap = generateWordArtBitmap(
                                        text = if (inputText.isNotBlank()) inputText else "OmniTool",
                                        gradient = WORD_GRADIENTS[selectedGradientIndex]
                                    )
                                    AppUtils.saveAndShareBitmap(context, bitmap, "Share Word Art")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Art Image")
                            }

                            FilledTonalButton(
                                onClick = { AppUtils.copyToClipboard(context, inputText, "Word Art") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Text")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun generateWordArtBitmap(text: String, gradient: List<Color>): Bitmap {
    val width = 1080
    val height = 600
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint().apply {
        isAntiAlias = true
        shader = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            gradient[0].hashCode(),
            gradient[1].hashCode(),
            android.graphics.Shader.TileMode.CLAMP
        )
    }
    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 36f, 36f, bgPaint)

    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    canvas.drawText(text, width / 2f, height / 2f + 10f, textPaint)

    val subPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 255, 255, 255)
        textAlign = Paint.Align.CENTER
        textSize = 28f
        isAntiAlias = true
    }
    canvas.drawText("★ CREATED WITH OMNITOOL STUDIO ★", width / 2f, height / 2f + 80f, subPaint)

    return bitmap
}
