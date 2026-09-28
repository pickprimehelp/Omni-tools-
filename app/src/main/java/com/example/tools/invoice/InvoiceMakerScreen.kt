package com.example.tools.invoice

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.InvoiceEntity
import com.example.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class InvoiceLineItem(
    val name: String,
    val qty: Double,
    val unitPrice: Double
) {
    val total: Double get() = qty * unitPrice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceMakerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val savedInvoices by db.invoiceDao().getAllInvoices().collectAsState(initial = emptyList())

    var activeTab by remember { mutableIntStateOf(0) } // 0: Create/Edit, 1: Saved History, 2: Expense Log

    // Invoice Form Fields
    var invoiceNumber by remember { mutableStateOf("INV-${System.currentTimeMillis() % 100000}") }
    var businessName by remember { mutableStateOf("Prime Global Solutions") }
    var businessPhone by remember { mutableStateOf("+91 98765 01234") }
    var businessAddress by remember { mutableStateOf("Tech Park, Suite 402, Bangalore") }
    var clientName by remember { mutableStateOf("Acme Corporation") }
    var clientPhone by remember { mutableStateOf("+91 91234 56789") }
    var clientAddress by remember { mutableStateOf("Commercial Complex, Mumbai") }
    var invoiceDate by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var dueDate by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() + 15L * 86400000L))) }
    var taxPercent by remember { mutableDoubleStateOf(18.0) }
    var discountPercent by remember { mutableDoubleStateOf(5.0) }
    var paymentStatus by remember { mutableStateOf("PENDING") }

    val lineItems = remember {
        mutableStateListOf(
            InvoiceLineItem("Mobile App UI/UX Design", 1.0, 12000.0),
            InvoiceLineItem("Cloud API Integration & Deployment", 1.0, 18000.0),
            InvoiceLineItem("Quality Testing & Play Store Release", 1.0, 8000.0)
        )
    }

    // Calculations
    val subtotal = lineItems.sumOf { it.total }
    val discountAmount = subtotal * (discountPercent / 100.0)
    val taxableAmount = subtotal - discountAmount
    val taxAmount = taxableAmount * (taxPercent / 100.0)
    val grandTotal = taxableAmount + taxAmount

    // Dialog state for adding line item
    var showAddItemDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1") }
    var newItemPrice by remember { mutableStateOf("1000") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice & Bill Maker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                // Save to Room DB
                                val itemsJsonArr = JSONArray()
                                lineItems.forEach {
                                    val obj = JSONObject()
                                    obj.put("name", it.name)
                                    obj.put("qty", it.qty)
                                    obj.put("price", it.unitPrice)
                                    itemsJsonArr.put(obj)
                                }

                                val entity = InvoiceEntity(
                                    invoiceNumber = invoiceNumber,
                                    clientName = clientName,
                                    clientPhone = clientPhone,
                                    clientAddress = clientAddress,
                                    date = invoiceDate,
                                    dueDate = dueDate,
                                    itemsJson = itemsJsonArr.toString(),
                                    subtotal = subtotal,
                                    taxPercent = taxPercent,
                                    discountPercent = discountPercent,
                                    total = grandTotal,
                                    status = paymentStatus
                                )
                                db.invoiceDao().insertInvoice(entity)

                                // Generate PDF and Share
                                val pdfFile = withContext(Dispatchers.IO) {
                                    generateInvoicePdf(
                                        context = context,
                                        invoiceNum = invoiceNumber,
                                        businessName = businessName,
                                        businessPhone = businessPhone,
                                        businessAddress = businessAddress,
                                        clientName = clientName,
                                        clientPhone = clientPhone,
                                        clientAddress = clientAddress,
                                        date = invoiceDate,
                                        dueDate = dueDate,
                                        items = lineItems,
                                        subtotal = subtotal,
                                        discountAmt = discountAmount,
                                        taxAmt = taxAmount,
                                        grandTotal = grandTotal,
                                        status = paymentStatus
                                    )
                                }
                                AppUtils.sharePdf(context, pdfFile, "Share Invoice $invoiceNumber")
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
                    text = { Text("Bill Editor") },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("History (${savedInvoices.size})") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
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
                        // Summary Hero Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    FilterChip(
                                        selected = paymentStatus == "PAID",
                                        onClick = { paymentStatus = if (paymentStatus == "PAID") "PENDING" else "PAID" },
                                        label = { Text(paymentStatus, fontWeight = FontWeight.Bold) }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text("Total Payable", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                        Text("₹${String.format(Locale.getDefault(), "%,.2f", grandTotal)}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Items: ${lineItems.size}", fontSize = 13.sp)
                                }
                            }
                        }

                        // Business Details Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Your Business Info", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                OutlinedTextField(
                                    value = businessName,
                                    onValueChange = { businessName = it },
                                    label = { Text("Business / Freelancer Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = businessPhone,
                                        onValueChange = { businessPhone = it },
                                        label = { Text("Phone") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = invoiceDate,
                                        onValueChange = { invoiceDate = it },
                                        label = { Text("Bill Date") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        // Client Details Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Billed To (Client)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                OutlinedTextField(
                                    value = clientName,
                                    onValueChange = { clientName = it },
                                    label = { Text("Client / Company Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = clientPhone,
                                        onValueChange = { clientPhone = it },
                                        label = { Text("Client Contact") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = dueDate,
                                        onValueChange = { dueDate = it },
                                        label = { Text("Due Date") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        // Line Items Header & List
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Products / Services (${lineItems.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            FilledTonalButton(
                                onClick = { showAddItemDialog = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Item")
                            }
                        }

                        lineItems.forEachIndexed { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${item.qty.toInt()} x ₹${item.unitPrice.toInt()}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("₹${item.total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        IconButton(onClick = { lineItems.removeAt(index) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        // Tax & Discount Settings
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = taxPercent.toString(),
                                onValueChange = { taxPercent = it.toDoubleOrNull() ?: 0.0 },
                                label = { Text("Tax / GST %") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = discountPercent.toString(),
                                onValueChange = { discountPercent = it.toDoubleOrNull() ?: 0.0 },
                                label = { Text("Discount %") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Bill Summary calculation breakdown
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Subtotal", color = Color.Gray, fontSize = 13.sp)
                                    Text("₹${String.format(Locale.getDefault(), "%,.2f", subtotal)}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Discount ($discountPercent%)", color = Color.Gray, fontSize = 13.sp)
                                    Text("-₹${String.format(Locale.getDefault(), "%,.2f", discountAmount)}", color = Color(0xFF388E3C), fontSize = 13.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Tax / GST ($taxPercent%)", color = Color.Gray, fontSize = 13.sp)
                                    Text("+₹${String.format(Locale.getDefault(), "%,.2f", taxAmount)}", fontSize = 13.sp)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("₹${String.format(Locale.getDefault(), "%,.2f", grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // History List
                    if (savedInvoices.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No saved invoices yet. Export an invoice to save here.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(savedInvoices) { _, inv ->
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
                                        Column {
                                            Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(inv.clientName, fontSize = 13.sp, color = Color.Gray)
                                            Text("Date: ${inv.date}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("₹${String.format(Locale.getDefault(), "%,.2f", inv.total)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                            IconButton(onClick = { coroutineScope.launch { db.invoiceDao().deleteInvoice(inv) } }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Item / Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newItemQty,
                            onValueChange = { newItemQty = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newItemPrice,
                            onValueChange = { newItemPrice = it },
                            label = { Text("Rate (₹)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = newItemQty.toDoubleOrNull() ?: 1.0
                        val rate = newItemPrice.toDoubleOrNull() ?: 0.0
                        if (newItemName.isNotBlank() && rate > 0) {
                            lineItems.add(InvoiceLineItem(newItemName, qty, rate))
                            newItemName = ""
                            newItemQty = "1"
                            newItemPrice = "1000"
                            showAddItemDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun generateInvoicePdf(
    context: android.content.Context,
    invoiceNum: String,
    businessName: String,
    businessPhone: String,
    businessAddress: String,
    clientName: String,
    clientPhone: String,
    clientAddress: String,
    date: String,
    dueDate: String,
    items: List<InvoiceLineItem>,
    subtotal: Double,
    discountAmt: Double,
    taxAmt: Double,
    grandTotal: Double,
    status: String
): File {
    val pdfDoc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 points
    val page = pdfDoc.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint().apply { isAntiAlias = true }

    // Header background bar
    paint.color = android.graphics.Color.parseColor("#1E293B")
    canvas.drawRect(0f, 0f, 595f, 100f, paint)

    // Business Name
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 22f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText(businessName, 36f, 48f, paint)

    paint.textSize = 11f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("$businessAddress • $businessPhone", 36f, 72f, paint)

    // Invoice Label & Number
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 20f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("TAX INVOICE", 559f, 48f, paint)
    paint.textSize = 12f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(invoiceNum, 559f, 72f, paint)

    paint.textAlign = Paint.Align.LEFT
    // Client & Dates Section
    var y = 140f
    paint.color = android.graphics.Color.BLACK
    paint.textSize = 11f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("BILLED TO:", 36f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("INVOICE DETAILS:", 559f, y, paint)

    y += 18f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText(clientName, 36f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Invoice Date: $date", 559f, y, paint)

    y += 16f
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("Contact: $clientPhone", 36f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Due Date: $dueDate", 559f, y, paint)

    y += 16f
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText(clientAddress, 36f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("Status: $status", 559f, y, paint)

    // Table Header
    y += 35f
    paint.color = android.graphics.Color.parseColor("#F1F5F9")
    canvas.drawRect(36f, y, 559f, y + 26f, paint)

    paint.color = android.graphics.Color.parseColor("#0F172A")
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("ITEM / DESCRIPTION", 44f, y + 18f, paint)
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("QTY", 380f, y + 18f, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("PRICE", 470f, y + 18f, paint)
    canvas.drawText("TOTAL", 550f, y + 18f, paint)

    // Items
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    items.forEach { item ->
        y += 26f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(item.name, 44f, y + 16f, paint)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("${item.qty.toInt()}", 380f, y + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("₹${item.unitPrice.toInt()}", 470f, y + 16f, paint)
        canvas.drawText("₹${item.total.toInt()}", 550f, y + 16f, paint)
    }

    // Divider
    y += 35f
    paint.strokeWidth = 1f
    paint.color = android.graphics.Color.LTGRAY
    canvas.drawLine(36f, y, 559f, y, paint)

    // Totals Breakdown
    y += 24f
    paint.textAlign = Paint.Align.RIGHT
    paint.color = android.graphics.Color.DKGRAY
    canvas.drawText("Subtotal:  ₹${String.format(Locale.getDefault(), "%,.2f", subtotal)}", 550f, y, paint)
    y += 18f
    canvas.drawText("Discount:  -₹${String.format(Locale.getDefault(), "%,.2f", discountAmt)}", 550f, y, paint)
    y += 18f
    canvas.drawText("Tax:  +₹${String.format(Locale.getDefault(), "%,.2f", taxAmt)}", 550f, y, paint)
    y += 22f
    paint.color = android.graphics.Color.parseColor("#0F172A")
    paint.textSize = 15f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Grand Total:  ₹${String.format(Locale.getDefault(), "%,.2f", grandTotal)}", 550f, y, paint)

    // Footer
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 10f
    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    paint.color = android.graphics.Color.GRAY
    canvas.drawText("Thank you for your business! Generated with OmniTool Studio", 297f, 800f, paint)

    pdfDoc.finishPage(page)

    val cachePath = File(context.cacheDir, "invoices")
    cachePath.mkdirs()
    val file = File(cachePath, "$invoiceNum.pdf")
    FileOutputStream(file).use { out ->
        pdfDoc.writeTo(out)
    }
    pdfDoc.close()
    return file
}
