package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.DisposableEffect

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


// ============================================================
// APP SCREENS
// ============================================================

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


// ============================================================
// TOOL MODEL
// ============================================================

data class ToolItem(
    val id: AppScreen,
    val title: String,
    val description: String,
    val category: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val dailySubIndex: Int = 0
)


// ============================================================
// ALL TOOLS
// ============================================================

val APP_TOOLS = listOf(

    ToolItem(
        id = AppScreen.WEDDING_CARD,
        title = "Wedding Card Maker",
        description = "Royal & modern invitation editor with photos, muhurat & HD export",
        category = "Creative",
        icon = Icons.Default.Favorite,
        gradient = listOf(
            Color(0xFFE91E63),
            Color(0xFFFF6090)
        )
    ),

    ToolItem(
        id = AppScreen.VIDEO_EDITOR,
        title = "Reels & Video Studio",
        description = "Short-form video maker with transition effects & synced audio",
        category = "Creative",
        icon = Icons.Default.VideoCameraBack,
        gradient = listOf(
            Color(0xFF7C4DFF),
            Color(0xFFB388FF)
        )
    ),

    ToolItem(
        id = AppScreen.BG_REMOVER,
        title = "Background Eraser HD",
        description = "Smart edge cutout & replace backdrop with studio colors or gradients",
        category = "Creative",
        icon = Icons.Default.AutoFixHigh,
        gradient = listOf(
            Color(0xFF00B0FF),
            Color(0xFF80D8FF)
        )
    ),

    ToolItem(
        id = AppScreen.DOC_SCANNER,
        title = "Doc Scanner & OCR",
        description = "Multi-page camera scan, text extraction & printable PDF export",
        category = "Business",
        icon = Icons.Default.DocumentScanner,
        gradient = listOf(
            Color(0xFF00BFA5),
            Color(0xFF64FFDA)
        )
    ),

    ToolItem(
        id = AppScreen.RESUME_MAKER,
        title = "Resume & CV Maker",
        description = "Professional CV builder with experience, skills & instant PDF export",
        category = "Business",
        icon = Icons.Default.Badge,
        gradient = listOf(
            Color(0xFF2563EB),
            Color(0xFF60A5FA)
        )
    ),

    ToolItem(
        id = AppScreen.AGE_CALCULATOR,
        title = "Age & Birthday Calc",
        description = "Exact age in years/months/days, next birthday countdown & zodiac",
        category = "Daily",
        icon = Icons.Default.Cake,
        gradient = listOf(
            Color(0xFF8B5CF6),
            Color(0xFFC4B5FD)
        )
    ),

    ToolItem(
        id = AppScreen.TEXT_DESIGN,
        title = "Fancy Text & Word Art",
        description = "20+ fancy unicode fonts, colorful word cards, borders & copy-share",
        category = "Creative",
        icon = Icons.Default.FormatSize,
        gradient = listOf(
            Color(0xFFEC4899),
            Color(0xFFF472B6)
        )
    ),

    ToolItem(
        id = AppScreen.VOICE_CHANGER,
        title = "Voice Changer Studio",
        description = "Robot, helium, deep monster, echo & speed effects audio recorder",
        category = "Creative",
        icon = Icons.Default.GraphicEq,
        gradient = listOf(
            Color(0xFFF59E0B),
            Color(0xFFFDE68A)
        )
    ),

    ToolItem(
        id = AppScreen.STATUS_MAKER,
        title = "Story & Status Maker",
        description = "9:16 social status with quotes, hindi shayari & typography presets",
        category = "Creative",
        icon = Icons.Default.AutoAwesome,
        gradient = listOf(
            Color(0xFFFF3D00),
            Color(0xFFFF9E80)
        )
    ),

    ToolItem(
        id = AppScreen.PHOTO_EDITOR,
        title = "Pro Photo Studio",
        description = "Color grading filters, brightness, contrast, crop & drawing brush",
        category = "Creative",
        icon = Icons.Default.PhotoFilter,
        gradient = listOf(
            Color(0xFF651FFF),
            Color(0xFFB388FF)
        )
    ),

    ToolItem(
        id = AppScreen.INVOICE_MAKER,
        title = "Invoice & Bill Maker",
        description = "Professional itemized bills, tax/discount calculation & PDF sharing",
        category = "Business",
        icon = Icons.Default.ReceiptLong,
        gradient = listOf(
            Color(0xFF2E7D32),
            Color(0xFF81C784)
        )
    ),

    ToolItem(
        id = AppScreen.WEATHER,
        title = "Weather Forecast",
        description = "Live real-time weather, 7-day forecast, wind, humidity & UV tracker",
        category = "Daily",
        icon = Icons.Default.WbSunny,
        gradient = listOf(
            Color(0xFFFF9100),
            Color(0xFFFFD180)
        )
    ),

    ToolItem(
        id = AppScreen.TASK_MANAGER,
        title = "Project Timelines",
        description = "Kanban board, team assignee tags, priority levels & progress",
        category = "Business",
        icon = Icons.Default.AssignmentTurnedIn,
        gradient = listOf(
            Color(0xFF304FFE),
            Color(0xFF8C9EFF)
        )
    ),

    ToolItem(
        id = AppScreen.URL_SHORTENER,
        title = "URL Shortener & QR",
        description = "Instant TinyURL generator, QR Code maker & click history",
        category = "Business",
        icon = Icons.Default.Link,
        gradient = listOf(
            Color(0xFF0091EA),
            Color(0xFF80D8FF)
        )
    ),

    ToolItem(
        id = AppScreen.CALCULATOR,
        title = "Scientific Calculator",
        description = "Trig functions, logarithms, powers & live currency exchange rates",
        category = "Daily",
        icon = Icons.Default.Calculate,
        gradient = listOf(
            Color(0xFF00897B),
            Color(0xFF4DB6AC)
        )
    ),

    ToolItem(
        id = AppScreen.DAILY_TOOLS,
        title = "QR Code Maker",
        description = "Custom QR codes for WiFi, WhatsApp, URLs, Text & Phone",
        category = "Daily",
        icon = Icons.Default.QrCode,
        gradient = listOf(
            Color(0xFF0284C7),
            Color(0xFF38BDF8)
        ),
        dailySubIndex = 0
    ),

    ToolItem(
        id = AppScreen.DAILY_TOOLS,
        title = "Everyday Essentials",
        description = "Unit Converter, Hydration & Habits, EMI/Discount, Notes",
        category = "Daily",
        icon = Icons.Default.Category,
        gradient = listOf(
            Color(0xFFD81B60),
            Color(0xFFFF4081)
        ),
        dailySubIndex = 1
    )
)


// ============================================================
// MAIN ACTIVITY
// ============================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AdMob
        com.example.util.AdMobManager.initialize(this)

        setContent {
            MyApplicationTheme {
                AppNavigator()
            }
        }
    }
}


// ============================================================
// APP NAVIGATION
// ============================================================

@Composable
fun AppNavigator() {

    var currentScreen by remember {
        mutableStateOf(AppScreen.HOME)
    }

    var dailySubIndex by remember {
        mutableIntStateOf(0)
    }

    var toolClickCount by remember {
        mutableIntStateOf(0)
    }

    val context = LocalContext.current
    val activity = context as? ComponentActivity


    // --------------------------------------------------------
    // Android Back Button
    // --------------------------------------------------------

    DisposableEffect(currentScreen, activity) {

        if (activity == null) {
            onDispose { }
        } else {

            val callback = object : OnBackPressedCallback(
                currentScreen != AppScreen.HOME
            ) {

                override fun handleOnBackPressed() {
                    currentScreen = AppScreen.HOME
                }
            }

            activity.onBackPressedDispatcher.addCallback(callback)

            onDispose {
                callback.remove()
            }
        }
    }


    // --------------------------------------------------------
    // Screen
