package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tools.age.AgeCalculatorScreen
import com.example.tools.bgremover.BackgroundRemoverScreen
import com.example.tools.calculator.CalculatorScreen
import com.example.tools.daily.DailyToolsScreen
import com.example.tools.invoice.InvoiceMakerScreen
import com.example.tools.photo.PhotoEditorScreen
import com.example.tools.resume.ResumeMakerScreen
import com.example.tools.scanner.DocScannerScreen
import com.example.tools.status.StatusMakerScreen
import com.example.tools.support.SupportAuthorScreen
import com.example.tools.tasks.TaskManagerScreen
import com.example.tools.textdesign.TextDesignScreen
import com.example.tools.url.UrlShortenerScreen
import com.example.tools.video.VideoEditorScreen
import com.example.tools.voice.VoiceChangerScreen
import com.example.tools.weather.WeatherScreen
import com.example.tools.wedding.WeddingCardMakerScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    HOME,
    WEDDING_CARD,
    VIDEO_EDITOR,
    BG_REMOVER,
    DOC_SCANNER,
    WEATHER,
    STATUS_MAKER,
    PHOTO_EDITOR,
    INVOICE_MAKER,
    TASK_MANAGER,
    URL_SHORTENER,
    CALCULATOR,
    DAILY_TOOLS,
    AGE_CALCULATOR,
    RESUME_MAKER,
    TEXT_DESIGN,
    VOICE_CHANGER,
    SUPPORT_AUTHOR
}

data class ToolItem(
    val id: AppScreen,
    val title: String,
    val description: String,
    val category: String, // "Creative", "Business", "Daily"
    val icon: ImageVector,
    val gradient: List<Color>,
    val dailySubIndex: Int = 0
)

val APP_TOOLS = listOf(
    ToolItem(
        id = AppScreen.WEDDING_CARD,
        title = "Wedding Card Maker",
        description = "Royal & modern invitation editor with photos, muhurat & HD export",
        category = "Creative",
        icon = Icons.Default.Favorite,
        gradient = listOf(Color(0xFFE91E63), Color(0xFFFF6090))
    ),
    ToolItem(
        id = AppScreen.VIDEO_EDITOR,
        title = "Reels & Video Studio",
        description = "Short-form video maker with transition effects & synced audio",
        category = "Creative",
        icon = Icons.Default.VideoCameraBack,
        gradient = listOf(Color(0xFF7C4DFF), Color(0xFFB388FF))
    ),
    ToolItem(
        id = AppScreen.BG_REMOVER,
        title = "Background Eraser HD",
        description = "Smart edge cutout & replace backdrop with studio colors or gradients",
        category = "Creative",
        icon = Icons.Default.AutoFixHigh,
        gradient = listOf(Color(0xFF00B0FF), Color(0xFF80D8FF))
    ),
    ToolItem(
        id = AppScreen.DOC_SCANNER,
        title = "Doc Scanner & OCR",
        description = "Multi-page camera scan, text extraction & printable PDF export",
        category = "Business",
        icon = Icons.Default.DocumentScanner,
        gradient = listOf(Color(0xFF00BFA5), Color(0xFF64FFDA))
    ),
    ToolItem(
        id = AppScreen.RESUME_MAKER,
        title = "Resume & CV Maker",
        description = "Professional CV builder with experience, skills & instant PDF export",
        category = "Business",
        icon = Icons.Default.Badge,
        gradient = listOf(Color(0xFF2563EB), Color(0xFF60A5FA))
    ),
    ToolItem(
        id = AppScreen.AGE_CALCULATOR,
        title = "Age & Birthday Calc",
        description = "Exact age in years/months/days, next birthday countdown & zodiac",
        category = "Daily",
        icon = Icons.Default.Cake,
        gradient = listOf(Color(0xFF8B5CF6), Color(0xFFC4B5FD))
    ),
    ToolItem(
        id = AppScreen.TEXT_DESIGN,
        title = "Fancy Text & Word Art",
        description = "20+ fancy unicode fonts, colorful word cards, borders & copy-share",
        category = "Creative",
        icon = Icons.Default.FormatSize,
        gradient = listOf(Color(0xFFEC4899), Color(0xFFF472B6))
    ),
    ToolItem(
        id = AppScreen.VOICE_CHANGER,
        title = "Voice Changer Studio",
        description = "Robot, helium, deep monster, echo & speed effects audio recorder",
        category = "Creative",
        icon = Icons.Default.GraphicEq,
        gradient = listOf(Color(0xFFF59E0B), Color(0xFFFDE68A))
    ),
    ToolItem(
        id = AppScreen.STATUS_MAKER,
        title = "Story & Status Maker",
        description = "9:16 social status with quotes, hindi shayari & typography presets",
        category = "Creative",
        icon = Icons.Default.AutoAwesome,
        gradient = listOf(Color(0xFFFF3D00), Color(0xFFFF9E80))
    ),
    ToolItem(
        id = AppScreen.PHOTO_EDITOR,
        title = "Pro Photo Studio",
        description = "Color grading filters, brightness, contrast, crop & drawing brush",
        category = "Creative",
        icon = Icons.Default.PhotoFilter,
        gradient = listOf(Color(0xFF651FFF), Color(0xFFB388FF))
    ),
    ToolItem(
        id = AppScreen.INVOICE_MAKER,
        title = "Invoice & Bill Maker",
        description = "Professional itemized bills, tax/discount calculation & PDF sharing",
        category = "Business",
        icon = Icons.Default.ReceiptLong,
        gradient = listOf(Color(0xFF2E7D32), Color(0xFF81C784))
    ),
    ToolItem(
        id = AppScreen.WEATHER,
        title = "Weather Forecast",
        description = "Live real-time weather, 7-day forecast, wind, humidity & UV tracker",
        category = "Daily",
        icon = Icons.Default.WbSunny,
        gradient = listOf(Color(0xFFFF9100), Color(0xFFFFD180))
    ),
    ToolItem(
        id = AppScreen.TASK_MANAGER,
        title = "Project Timelines",
        description = "Kanban board, team assignee tags, priority levels & progress",
        category = "Business",
        icon = Icons.Default.AssignmentTurnedIn,
        gradient = listOf(Color(0xFF304FFE), Color(0xFF8C9EFF))
    ),
    ToolItem(
        id = AppScreen.URL_SHORTENER,
        title = "URL Shortener & QR",
        description = "Instant TinyURL generator, QR Code maker & click history",
        category = "Business",
        icon = Icons.Default.Link,
        gradient = listOf(Color(0xFF0091EA), Color(0xFF80D8FF))
    ),
    ToolItem(
        id = AppScreen.CALCULATOR,
        title = "Scientific Calculator",
        description = "Trig functions, logarithms, powers & live currency exchange rates",
        category = "Daily",
        icon = Icons.Default.Calculate,
        gradient = listOf(Color(0xFF00897B), Color(0xFF4DB6AC))
    ),
    ToolItem(
        id = AppScreen.DAILY_TOOLS,
        title = "QR Code Maker",
        description = "Custom QR codes for WiFi, WhatsApp, URLs, Text & Phone",
        category = "Daily",
        icon = Icons.Default.QrCode,
        gradient = listOf(Color(0xFF0284C7), Color(0xFF38BDF8)),
        dailySubIndex = 0
    ),
    ToolItem(
        id = AppScreen.DAILY_TOOLS,
        title = "Everyday Essentials",
        description = "Unit Converter, Hydration & Habits, EMI/Discount, Notes",
        category = "Daily",
        icon = Icons.Default.Category,
        gradient = listOf(Color(0xFFD81B60), Color(0xFFFF4081)),
        dailySubIndex = 1
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.AdMobManager.initialize(this)
        setContent {
            MyApplicationTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var dailySubIndex by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    var toolClickCount by remember { mutableIntStateOf(0) }

    // Android back handling
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = AppScreen.HOME
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState != AppScreen.HOME) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it / 2 } + fadeOut()
            } else {
                slideInHorizontally { -it / 2 } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        },
        label = "ScreenTransition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(
                onNavigate = { tool ->
                    toolClickCount++
                    val activity = context as? Activity
                    if (toolClickCount % 3 == 0 && activity != null) {
                        com.example.util.AdMobManager.showInterstitial(activity) {
                            if (tool.id == AppScreen.DAILY_TOOLS) {
                                dailySubIndex = tool.dailySubIndex
                            }
                            currentScreen = tool.id
                        }
                    } else {
                        if (tool.id == AppScreen.DAILY_TOOLS) {
                            dailySubIndex = tool.dailySubIndex
                        }
                        currentScreen = tool.id
                    }
                },
                onOpenSupport = {
                    currentScreen = AppScreen.SUPPORT_AUTHOR
                }
            )
            AppScreen.WEDDING_CARD -> WeddingCardMakerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.VIDEO_EDITOR -> VideoEditorScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.BG_REMOVER -> BackgroundRemoverScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.DOC_SCANNER -> DocScannerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.WEATHER -> WeatherScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.STATUS_MAKER -> StatusMakerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.PHOTO_EDITOR -> PhotoEditorScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.INVOICE_MAKER -> InvoiceMakerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.TASK_MANAGER -> TaskManagerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.URL_SHORTENER -> UrlShortenerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.CALCULATOR -> CalculatorScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.DAILY_TOOLS -> DailyToolsScreen(initialSubTool = dailySubIndex, onBack = { currentScreen = AppScreen.HOME })
            AppScreen.AGE_CALCULATOR -> AgeCalculatorScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.RESUME_MAKER -> ResumeMakerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.TEXT_DESIGN -> TextDesignScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.VOICE_CHANGER -> VoiceChangerScreen(onBack = { currentScreen = AppScreen.HOME })
            AppScreen.SUPPORT_AUTHOR -> SupportAuthorScreen(onBack = { currentScreen = AppScreen.HOME })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (ToolItem) -> Unit,
    onOpenSupport: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredTools = APP_TOOLS.filter { tool ->
        val matchesCategory = (selectedCategory == "All") || (tool.category == selectedCategory)
        val matchesSearch = searchQuery.isBlank() ||
                tool.title.contains(searchQuery, ignoreCase = true) ||
                tool.description.contains(searchQuery, ignoreCase = true) ||
                tool.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Widgets, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("OmniTool", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = onOpenSupport,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFFE4E6),
                            contentColor = Color(0xFFE11D48)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Support", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            com.example.util.AdMobBannerView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search any tool (Age, Resume, Voice, Wedding, PDF)...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_tools_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp)
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Creative", "Business", "Daily").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                when (cat) {
                                    "All" -> "All Tools (${APP_TOOLS.size})"
                                    "Creative" -> "Design & Media"
                                    "Business" -> "Business & Docs"
                                    else -> "Daily Essentials"
                                },
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }

            // Tools Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(filteredTools, key = { it.title }) { tool ->
                    ToolGridCard(tool = tool, onClick = { onNavigate(tool) })
                }

                // Dedicated Footer: Fund & Support Author Banner
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 20.dp)
                            .clickable { onOpenSupport() },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFFB7185)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Support App Developer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("❤️", fontSize = 12.sp)
                                    }
                                    Text("Enjoying free tools? Scan author QR to donate & fund!", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            FilledTonalButton(
                                onClick = onOpenSupport,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFE11D48),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Fund / QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolGridCard(tool: ToolItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("tool_card_${tool.id.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(tool.gradient)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(tool.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }

                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(tool.category, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Column {
                Text(
                    text = tool.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open Tool", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
