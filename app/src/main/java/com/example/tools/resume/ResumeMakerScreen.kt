package com.example.tools.resume

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class Experience(val role: String, val company: String, val duration: String, val desc: String)
data class Education(val degree: String, val school: String, val year: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeMakerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Form, 1: Preview

    // Personal details
    var fullName by remember { mutableStateOf("Rahul Verma") }
    var jobTitle by remember { mutableStateOf("Senior Android Developer") }
    var email by remember { mutableStateOf("rahul.verma@example.com") }
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var location by remember { mutableStateOf("Bangalore, India") }
    var summary by remember { mutableStateOf("Passionate Mobile Engineer with 4+ years of expertise in Kotlin, Jetpack Compose, Clean Architecture, and shipping high-performance applications to Google Play Store.") }

    // Experiences
    val experiences = remember {
        mutableStateListOf(
            Experience("Lead Android Engineer", "TechNova Labs", "2023 - Present", "Architected scalable Compose UI and improved app crash-free rate to 99.8%."),
            Experience("Android Software Developer", "Global Innovations", "2021 - 2023", "Developed native payment integration, Room caching, and offline-first features.")
        )
    }

    // Education
    val educations = remember {
        mutableStateListOf(
            Education("B.Tech in Computer Science", "National Institute of Technology", "2017 - 2021")
        )
    }

    // Skills
    val skills = remember {
        mutableStateListOf("Kotlin", "Jetpack Compose", "Coroutines & Flow", "Room Database", "Retrofit", "Git & CI/CD", "Material Design 3")
    }

    var newSkill by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume & CV Builder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                val pdfFile = withContext(Dispatchers.IO) {
                                    generateResumePdf(
                                        context = context,
                                        fullName = fullName,
                                        title = jobTitle,
                                        email = email,
                                        phone = phone,
                                        location = location,
                                        summary = summary,
                                        experiences = experiences,
                                        educations = educations,
                                        skills = skills
                                    )
                                }
                                AppUtils.sharePdf(context, pdfFile, "Share Resume CV")
                            }
                        }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Edit Details") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Live CV Preview") },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = null) }
                )
            }

            when (activeTab) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Personal Information Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Personal Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = jobTitle,
                                    onValueChange = { jobTitle = it },
                                    label = { Text("Professional Headline / Job Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Phone") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    label = { Text("Location (City, Country)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = summary,
                                    onValueChange = { summary = it },
                                    label = { Text("Professional Summary / About Me") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4
                                )
                            }
                        }

                        // Work Experience
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Work Experience", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                experiences.forEachIndexed { index, exp ->
                                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(exp.role, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                IconButton(onClick = { experiences.removeAt(index) }) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                                                }
                                            }
                                            Text("${exp.company} • ${exp.duration}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            Text(exp.desc, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Skills
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Skills & Expertise", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newSkill,
                                        onValueChange = { newSkill = it },
                                        placeholder = { Text("Add skill (e.g. Flutter, SEO)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Button(onClick = {
                                        if (newSkill.isNotBlank()) {
                                            skills.add(newSkill.trim())
                                            newSkill = ""
                                        }
                                    }) {
                                        Text("Add")
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    skills.forEach { skill ->
                                        AssistChip(
                                            onClick = { skills.remove(skill) },
                                            label = { Text(skill, fontSize = 11.sp) },
                                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Live Printable CV Preview
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.70f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // CV Header
                                Column {
                                    Text(fullName, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF0F172A))
                                    Text(jobTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF4F46E5))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$email • $phone • $location", fontSize = 10.sp, color = Color(0xFF64748B))
                                }

                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                // Summary
                                Column {
                                    Text("PROFILE SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(summary, fontSize = 10.sp, color = Color(0xFF334155), lineHeight = 14.sp)
                                }

                                // Experience
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("WORK EXPERIENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), letterSpacing = 1.sp)
                                    experiences.forEach { exp ->
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(exp.role, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
                                                Text(exp.duration, fontSize = 10.sp, color = Color(0xFF64748B))
                                            }
                                            Text(exp.company, fontSize = 10.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)
                                            Text(exp.desc, fontSize = 9.sp, color = Color(0xFF475569))
                                        }
                                    }
                                }

                                // Skills
                                Column {
                                    Text("TECHNICAL SKILLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), letterSpacing = 1.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(skills.joinToString(" • "), fontSize = 10.sp, color = Color(0xFF334155))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun generateResumePdf(
    context: android.content.Context,
    fullName: String,
    title: String,
    email: String,
    phone: String,
    location: String,
    summary: String,
    experiences: List<Experience>,
    educations: List<Education>,
    skills: List<String>
): File {
    val pdfDoc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
    val page = pdfDoc.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply { isAntiAlias = true }

    // Top Header Banner
    paint.color = android.graphics.Color.parseColor("#1E293B")
    canvas.drawRect(0f, 0f, 595f, 110f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 24f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(fullName, 40f, 50f, paint)

    paint.color = android.graphics.Color.parseColor("#818CF8")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(title, 40f, 72f, paint)

    paint.color = android.graphics.Color.parseColor("#CBD5E1")
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("$email  |  $phone  |  $location", 40f, 92f, paint)

    var y = 145f

    // Summary Section
    paint.color = android.graphics.Color.parseColor("#0F172A")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("PROFILE SUMMARY", 40f, y, paint)

    y += 18f
    paint.color = android.graphics.Color.parseColor("#334155")
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    val summaryWords = summary.split(" ")
    var line = ""
    for (w in summaryWords) {
        val test = if (line.isEmpty()) w else "$line $w"
        if (paint.measureText(test) > 515f) {
            canvas.drawText(line, 40f, y, paint)
            y += 14f
            line = w
        } else {
            line = test
        }
    }
    if (line.isNotEmpty()) {
        canvas.drawText(line, 40f, y, paint)
        y += 24f
    }

    // Work Experience
    paint.color = android.graphics.Color.parseColor("#0F172A")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("WORK EXPERIENCE", 40f, y, paint)
    y += 20f

    experiences.forEach { exp ->
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(exp.role, 40f, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = android.graphics.Color.GRAY
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(exp.duration, 555f, y, paint)
        paint.textAlign = Paint.Align.LEFT

        y += 14f
        paint.color = android.graphics.Color.parseColor("#4F46E5")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(exp.company, 40f, y, paint)

        y += 14f
        paint.color = android.graphics.Color.parseColor("#475569")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(exp.desc, 40f, y, paint)
        y += 20f
    }

    // Technical Skills
    y += 10f
    paint.color = android.graphics.Color.parseColor("#0F172A")
    paint.textSize = 13f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("KEY SKILLS & COMPETENCIES", 40f, y, paint)
    y += 18f

    paint.color = android.graphics.Color.parseColor("#334155")
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(skills.joinToString("  •  "), 40f, y, paint)

    pdfDoc.finishPage(page)

    val cacheDir = File(context.cacheDir, "resumes")
    cacheDir.mkdirs()
    val file = File(cacheDir, "Resume_${System.currentTimeMillis()}.pdf")
    FileOutputStream(file).use { out ->
        pdfDoc.writeTo(out)
    }
    pdfDoc.close()
    return file
}
