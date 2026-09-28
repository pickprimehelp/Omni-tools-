package com.example.tools.daily

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.NoteEntity
import com.example.util.AppUtils
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.launch
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyToolsScreen(initialSubTool: Int = 0, onBack: () -> Unit) {
    var selectedTool by remember { mutableIntStateOf(initialSubTool) }
    // 0: QR Generator, 1: Unit Converter, 2: Hydration & Habits, 3: Discount & EMI, 4: Quick Notes

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Essentials", fontWeight = FontWeight.Bold) },
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
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTool, edgePadding = 16.dp) {
                Tab(
                    selected = selectedTool == 0,
                    onClick = { selectedTool = 0 },
                    text = { Text("QR Generator") },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                )
                Tab(
                    selected = selectedTool == 1,
                    onClick = { selectedTool = 1 },
                    text = { Text("Unit Converter") },
                    icon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                )
                Tab(
                    selected = selectedTool == 2,
                    onClick = { selectedTool = 2 },
                    text = { Text("Water & Habits") },
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null) }
                )
                Tab(
                    selected = selectedTool == 3,
                    onClick = { selectedTool = 3 },
                    text = { Text("Discount & EMI") },
                    icon = { Icon(Icons.Default.Percent, contentDescription = null) }
                )
                Tab(
                    selected = selectedTool == 4,
                    onClick = { selectedTool = 4 },
                    text = { Text("Quick Notes") },
                    icon = { Icon(Icons.Default.NoteAlt, contentDescription = null) }
                )
            }

            when (selectedTool) {
                0 -> QrGeneratorView()
                1 -> UnitConverterView()
                2 -> WaterAndHabitsView()
                3 -> DiscountAndEmiView()
                4 -> QuickNotesView()
            }
        }
    }
}

@Composable
fun QrGeneratorView() {
    val context = LocalContext.current
    var qrType by remember { mutableStateOf("Text") } // Text, WiFi, WhatsApp, URL
    var inputMain by remember { mutableStateOf("Welcome to OmniTool Studio!") }
    var wifiSsid by remember { mutableStateOf("MyHomeWiFi") }
    var wifiPass by remember { mutableStateOf("password123") }
    var phoneNum by remember { mutableStateOf("+919876543210") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun buildContent(): String {
        return when (qrType) {
            "WiFi" -> "WIFI:T:WPA;S:$wifiSsid;P:$wifiPass;;"
            "WhatsApp" -> "https://wa.me/${phoneNum.replace("+", "").replace(" ", "")}"
            else -> inputMain
        }
    }

    LaunchedEffect(qrType, inputMain, wifiSsid, wifiPass, phoneNum) {
        val payload = buildContent()
        if (payload.isNotBlank()) {
            qrBitmap = QrCodeGenerator.generateQrBitmap(payload, size = 450)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // QR Type Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Text", "URL", "WhatsApp", "WiFi").forEach { t ->
                FilterChip(
                    selected = qrType == t,
                    onClick = { qrType = t },
                    label = { Text(t, fontSize = 12.sp) }
                )
            }
        }

        // Inputs based on type
        when (qrType) {
            "WiFi" -> {
                OutlinedTextField(
                    value = wifiSsid,
                    onValueChange = { wifiSsid = it },
                    label = { Text("Wi-Fi Network Name (SSID)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = wifiPass,
                    onValueChange = { wifiPass = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            "WhatsApp" -> {
                OutlinedTextField(
                    value = phoneNum,
                    onValueChange = { phoneNum = it },
                    label = { Text("Phone Number with Country Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            else -> {
                OutlinedTextField(
                    value = inputMain,
                    onValueChange = { inputMain = it },
                    label = { Text(if (qrType == "URL") "Website URL" else "Text Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        }

        // QR Code Display Card
        if (qrBitmap != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { AppUtils.saveAndShareBitmap(context, qrBitmap!!, "Share QR Code") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share QR")
                        }

                        FilledTonalButton(
                            onClick = { AppUtils.copyToClipboard(context, buildContent(), "QR Content") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Data")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitConverterView() {
    var category by remember { mutableStateOf("Length") } // Length, Mass, Temp, Area, Speed
    var inputValue by remember { mutableStateOf("1") }

    val categories = listOf("Length", "Mass", "Temperature", "Area", "Speed")

    val lengthUnits = listOf("Meters", "Kilometers", "Centimeters", "Feet", "Inches", "Miles")
    val massUnits = listOf("Kilograms", "Grams", "Pounds", "Ounces", "Tons")
    val tempUnits = listOf("Celsius", "Fahrenheit", "Kelvin")
    val speedUnits = listOf("km/h", "mph", "m/s", "knots")

    val currentUnits = when (category) {
        "Mass" -> massUnits
        "Temperature" -> tempUnits
        "Speed" -> speedUnits
        else -> lengthUnits
    }

    var fromUnit by remember { mutableStateOf(currentUnits[0]) }
    var toUnit by remember { mutableStateOf(currentUnits[1]) }

    LaunchedEffect(category) {
        fromUnit = currentUnits[0]
        toUnit = currentUnits.getOrElse(1) { currentUnits[0] }
    }

    val inputNum = inputValue.toDoubleOrNull() ?: 0.0

    fun convert(): Double {
        if (category == "Temperature") {
            val c = when (fromUnit) {
                "Fahrenheit" -> (inputNum - 32) * 5 / 9
                "Kelvin" -> inputNum - 273.15
                else -> inputNum
            }
            return when (toUnit) {
                "Fahrenheit" -> c * 9 / 5 + 32
                "Kelvin" -> c + 273.15
                else -> c
            }
        }

        // Convert to base unit then to target
        val toBase = when (fromUnit) {
            "Kilometers" -> inputNum * 1000.0
            "Centimeters" -> inputNum * 0.01
            "Feet" -> inputNum * 0.3048
            "Inches" -> inputNum * 0.0254
            "Miles" -> inputNum * 1609.34
            "Grams" -> inputNum * 0.001
            "Pounds" -> inputNum * 0.453592
            "Ounces" -> inputNum * 0.0283495
            "Tons" -> inputNum * 1000.0
            "mph" -> inputNum * 1.60934
            "m/s" -> inputNum * 3.6
            "knots" -> inputNum * 1.852
            else -> inputNum
        }

        return when (toUnit) {
            "Kilometers" -> toBase / 1000.0
            "Centimeters" -> toBase / 0.01
            "Feet" -> toBase / 0.3048
            "Inches" -> toBase / 0.0254
            "Miles" -> toBase / 1609.34
            "Grams" -> toBase / 0.001
            "Pounds" -> toBase / 0.453592
            "Ounces" -> toBase / 0.0283495
            "Tons" -> toBase / 1000.0
            "mph" -> toBase / 1.60934
            "m/s" -> toBase / 3.6
            "knots" -> toBase / 1.852
            else -> toBase
        }
    }

    val convertedValue = convert()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Category chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) }
                )
            }
        }

        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Result", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                Text(
                    "${String.format(java.util.Locale.getDefault(), "%,.4f", convertedValue)} $toUnit",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("$inputValue $fromUnit = $convertedValue $toUnit", fontSize = 13.sp)
            }
        }

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Input Value") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("From", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                currentUnits.take(4).forEach { u ->
                    FilterChip(
                        selected = fromUnit == u,
                        onClick = { fromUnit = u },
                        label = { Text(u, fontSize = 11.sp) }
                    )
                }
            }

            IconButton(onClick = {
                val tmp = fromUnit
                fromUnit = toUnit
                toUnit = tmp
            }) {
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap")
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("To", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                currentUnits.take(4).forEach { u ->
                    FilterChip(
                        selected = toUnit == u,
                        onClick = { toUnit = u },
                        label = { Text(u, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}

@Composable
fun WaterAndHabitsView() {
    var waterGlasses by remember { mutableIntStateOf(4) }
    val maxGlasses = 8
    val habits = remember {
        mutableStateListOf(
            "Morning Walk / Jogging (30 mins)" to true,
            "Drink 2 Litres of Water" to false,
            "Read 15 Pages of a Book" to true,
            "Meditation / Deep Breathing (10 mins)" to false,
            "Plan Next Day Priorities" to false
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Water Tracker Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Hydration Tracker", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF006064))
                        Text("Target: 2000 ml (8 glasses)", fontSize = 12.sp, color = Color(0xFF00838F))
                    }
                    Text("${waterGlasses * 250} / 2000 ml", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF006064))
                }

                LinearProgressIndicator(
                    progress = { (waterGlasses.toFloat() / maxGlasses).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = Color(0xFF00ACC1),
                    trackColor = Color(0xFFB2EBF2)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$waterGlasses of $maxGlasses Glasses", fontWeight = FontWeight.SemiBold, color = Color(0xFF006064))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { if (waterGlasses > 0) waterGlasses-- },
                            modifier = Modifier.background(Color.White, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFF006064))
                        }
                        Button(
                            onClick = { if (waterGlasses < 12) waterGlasses++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F))
                        ) {
                            Icon(Icons.Default.LocalDrink, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+1 Glass")
                        }
                    }
                }
            }
        }

        // Daily Habits Checklist
        Text("Daily Habits Streak", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        habits.forEachIndexed { index, habit ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = habit.second,
                        onCheckedChange = { isChecked ->
                            habits[index] = habit.first to isChecked
                        }
                    )
                    Text(
                        text = habit.first,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (habit.second) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun DiscountAndEmiView() {
    var toolMode by remember { mutableIntStateOf(0) } // 0: Discount, 1: Loan EMI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = toolMode == 0,
                onClick = { toolMode = 0 },
                label = { Text("Shopping Discount") },
                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = toolMode == 1,
                onClick = { toolMode = 1 },
                label = { Text("Loan EMI Calc") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (toolMode == 0) {
            var origPrice by remember { mutableStateOf("2500") }
            var discountRate by remember { mutableStateOf("30") }

            val original = origPrice.toDoubleOrNull() ?: 0.0
            val disc = discountRate.toDoubleOrNull() ?: 0.0
            val savedAmount = original * (disc / 100.0)
            val finalPrice = original - savedAmount

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Final Discounted Price", fontSize = 13.sp)
                    Text(
                        "₹${String.format(java.util.Locale.getDefault(), "%,.2f", finalPrice)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "You Save: ₹${String.format(java.util.Locale.getDefault(), "%,.2f", savedAmount)} (${disc.toInt()}%)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            OutlinedTextField(
                value = origPrice,
                onValueChange = { origPrice = it },
                label = { Text("Original Price (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = discountRate,
                onValueChange = { discountRate = it },
                label = { Text("Discount Percentage (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            var loanAmount by remember { mutableStateOf("500000") }
            var annualInterest by remember { mutableStateOf("8.5") }
            var tenureYears by remember { mutableStateOf("5") }

            val p = loanAmount.toDoubleOrNull() ?: 0.0
            val r = (annualInterest.toDoubleOrNull() ?: 0.0) / (12 * 100)
            val n = (tenureYears.toDoubleOrNull() ?: 0.0) * 12

            val emi = if (p > 0 && r > 0 && n > 0) {
                (p * r * (1 + r).pow(n)) / ((1 + r).pow(n) - 1)
            } else 0.0
            val totalPayment = emi * n
            val totalInterest = totalPayment - p

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monthly EMI Payment", fontSize = 13.sp)
                    Text(
                        "₹${String.format(java.util.Locale.getDefault(), "%,.2f", emi)} / mo",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Interest: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalInterest)}", fontSize = 12.sp)
                        Text("Total Pay: ₹${String.format(java.util.Locale.getDefault(), "%,.0f", totalPayment)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedTextField(
                value = loanAmount,
                onValueChange = { loanAmount = it },
                label = { Text("Loan Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = annualInterest,
                    onValueChange = { annualInterest = it },
                    label = { Text("Interest Rate (%)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tenureYears,
                    onValueChange = { tenureYears = it },
                    label = { Text("Tenure (Years)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun QuickNotesView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val notes by db.noteDao().getAllNotes().collectAsState(initial = emptyList())

    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#1E293B") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add note card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newContent,
                    onValueChange = { newContent = it },
                    label = { Text("Take a quick note...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Button(
                    onClick = {
                        if (newTitle.isNotBlank() || newContent.isNotBlank()) {
                            coroutineScope.launch {
                                db.noteDao().insertNote(
                                    NoteEntity(
                                        title = if (newTitle.isNotBlank()) newTitle else "Untitled",
                                        content = newContent,
                                        colorHex = selectedColor
                                    )
                                )
                                newTitle = ""
                                newContent = ""
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Note")
                }
            }
        }

        // Notes List
        Text("Saved Notes (${notes.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(notes, key = { it.id }) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            IconButton(onClick = { coroutineScope.launch { db.noteDao().deleteNote(note) } }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                        if (note.content.isNotBlank()) {
                            Text(note.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}
