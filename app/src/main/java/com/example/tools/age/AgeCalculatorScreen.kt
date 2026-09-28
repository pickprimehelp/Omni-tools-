package com.example.tools.age

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Default birth date: 15 August 2000
    var birthDay by remember { mutableIntStateOf(15) }
    var birthMonth by remember { mutableIntStateOf(8) } // 1-12
    var birthYear by remember { mutableIntStateOf(2000) }

    // Calculate age metrics
    val now = Calendar.getInstance()
    val birthCal = Calendar.getInstance().apply {
        set(birthYear, birthMonth - 1, birthDay, 0, 0, 0)
    }

    val diffMillis = maxOf(0L, now.timeInMillis - birthCal.timeInMillis)
    val totalDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    val totalHours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)

    // Accurate Year, Month, Day calculation
    var years = now.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
    var months = now.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
    var days = now.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

    if (days < 0) {
        months--
        val prevMonth = (now.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    if (months < 0) {
        years--
        months += 12
    }

    // Next Birthday calculation
    val nextBday = Calendar.getInstance().apply {
        set(now.get(Calendar.YEAR), birthMonth - 1, birthDay)
        if (before(now)) {
            add(Calendar.YEAR, 1)
        }
    }
    val daysUntilNextBday = TimeUnit.MILLISECONDS.toDays(nextBday.timeInMillis - now.timeInMillis)
    val dayOfWeekNextBday = SimpleDateFormat("EEEE", Locale.getDefault()).format(nextBday.time)

    // Fun facts
    val heartBeats = totalDays * 100000L
    val breathsTaken = totalDays * 21000L
    val sleepingDays = totalDays / 3

    // Zodiac sign
    fun getZodiacSign(day: Int, month: Int): Pair<String, String> {
        return when (month) {
            1 -> if (day < 20) "Capricorn ♑" to "Ambitious & Grounded" else "Aquarius ♒" to "Visionary & Free"
            2 -> if (day < 19) "Aquarius ♒" to "Visionary & Free" else "Pisces ♓" to "Intuitive & Artistic"
            3 -> if (day < 21) "Pisces ♓" to "Intuitive & Artistic" else "Aries ♈" to "Energetic & Bold"
            4 -> if (day < 20) "Aries ♈" to "Energetic & Bold" else "Taurus ♉" to "Loyal & Determined"
            5 -> if (day < 21) "Taurus ♉" to "Loyal & Determined" else "Gemini ♊" to "Curious & Adaptable"
            6 -> if (day < 21) "Gemini ♊" to "Curious & Adaptable" else "Cancer ♋" to "Protective & Empathetic"
            7 -> if (day < 23) "Cancer ♋" to "Protective & Empathetic" else "Leo ♌" to "Confident & Passionate"
            8 -> if (day < 23) "Leo ♌" to "Confident & Passionate" else "Virgo ♍" to "Analytical & Kind"
            9 -> if (day < 23) "Virgo ♍" to "Analytical & Kind" else "Libra ♎" to "Charming & Balanced"
            10 -> if (day < 23) "Libra ♎" to "Charming & Balanced" else "Scorpio ♏" to "Intense & Magnetic"
            11 -> if (day < 22) "Scorpio ♏" to "Intense & Magnetic" else "Sagittarius ♐" to "Adventurous & Honest"
            else -> if (day < 22) "Sagittarius ♐" to "Adventurous & Honest" else "Capricorn ♑" to "Ambitious & Grounded"
        }
    }
    val (zodiacName, zodiacTrait) = getZodiacSign(birthDay, birthMonth)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Age & Birthday Tracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            val shareContent = """
                                🎂 My Age Statistics:
                                • Age: $years Years, $months Months, $days Days
                                • Total Days Lived: $totalDays Days
                                • Next Birthday: In $daysUntilNextBday days (on a $dayOfWeekNextBday)
                                • Zodiac Sign: $zodiacName
                                Calculated using OmniTool Studio!
                            """.trimIndent()
                            AppUtils.shareText(context, shareContent, "Share Age Stats")
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
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
            // Hero Age Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))))
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CURRENT AGE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f), letterSpacing = 2.sp)
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(zodiacName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Big Years Number
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$years",
                                fontSize = 56.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Years Old",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AgeSubBadge("$months", "Months")
                            AgeSubBadge("$days", "Days")
                            AgeSubBadge("${totalDays / 7}", "Weeks")
                        }
                    }
                }
            }

            // Birth Date Selector Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Date of Birth", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = birthDay.toString(),
                            onValueChange = { birthDay = (it.toIntOrNull() ?: 1).coerceIn(1, 31) },
                            label = { Text("Day") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = birthMonth.toString(),
                            onValueChange = { birthMonth = (it.toIntOrNull() ?: 1).coerceIn(1, 12) },
                            label = { Text("Month") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = birthYear.toString(),
                            onValueChange = { birthYear = (it.toIntOrNull() ?: 2000).coerceIn(1900, 2026) },
                            label = { Text("Year") },
                            modifier = Modifier.weight(1.3f),
                            singleLine = true
                        )
                    }
                }
            }

            // Next Birthday Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Next Birthday", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Celebration on a $dayOfWeekNextBday", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$daysUntilNextBday", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Days Left", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Life Statistics & Fun Facts
            Text("Life Journey Statistics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LifeStatCard("Days Lived", String.format(Locale.getDefault(), "%,d", totalDays), Icons.Default.CalendarToday, Modifier.weight(1f))
                LifeStatCard("Hours Lived", String.format(Locale.getDefault(), "%,d", totalHours), Icons.Default.Schedule, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LifeStatCard("Heartbeats", "~${String.format(Locale.getDefault(), "%,d", heartBeats / 1000000)} Million", Icons.Default.Favorite, Modifier.weight(1f))
                LifeStatCard("Sleep Time", "~$sleepingDays Days", Icons.Default.Bedtime, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AgeSubBadge(value: String, label: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

@Composable
fun LifeStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(title, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
