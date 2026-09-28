package com.example.tools.support

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.util.AppUtils
import com.example.util.QrCodeGenerator
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportAuthorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("omnitool_support_prefs", Context.MODE_PRIVATE) }

    var authorUpiId by remember { mutableStateOf(prefs.getString("author_upi_id", "pickprimehelp@okaxis") ?: "pickprimehelp@okaxis") }
    var authorName by remember { mutableStateOf(prefs.getString("author_name", "OmniTool Author") ?: "OmniTool Author") }
    var customQrImagePath by remember { mutableStateOf(prefs.getString("author_qr_path", null)) }
    var selectedAmount by remember { mutableStateOf("100") }
    var customAmount by remember { mutableStateOf("") }
    var showEditUpiDialog by remember { mutableStateOf(false) }

    val effectiveAmount = if (customAmount.isNotBlank()) customAmount else selectedAmount

    // UPI Deep Link payload
    val upiUriString = "upi://pay?pa=$authorUpiId&pn=${Uri.encode(authorName)}&am=$effectiveAmount&cu=INR&tn=Support%20OmniTool%20Development"

    var generatedQrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(authorUpiId, effectiveAmount) {
        generatedQrBitmap = QrCodeGenerator.generateQrBitmap(upiUriString, size = 480)
    }

    // Zero-permission Photo Picker for Google Play Policy compliance
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "author_qr_scanner.png")
                FileOutputStream(targetFile).use { output ->
                    inputStream?.copyTo(output)
                }
                customQrImagePath = targetFile.absolutePath
                prefs.edit().putString("author_qr_path", targetFile.absolutePath).apply()
                Toast.makeText(context, "Aapka personal QR Scanner set ho gaya!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving QR: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Developer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditUpiDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Configure UPI ID")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Appreciation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFFB7185))))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Text(
                            text = "Support OmniTool Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Yeh app sabhi users ke liye 100% free hai. Agar aapko yeh app pasand aaya, toh aap apni khushi se app developer ko fund / donate kar sakte hain!",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // QR Code Scanner Container with Cyan/Navy Rounded Frame
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Scan with Any UPI App (GPay, PhonePe, Paytm)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    // Dual-Tone Border Frame matching user's Paytm scanner
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF00B5E2), // Vibrant Cyan top half
                                        Color(0xFF00B5E2),
                                        Color(0xFF00296B), // Deep Navy bottom half
                                        Color(0xFF00296B)
                                    )
                                )
                            )
                            .padding(14.dp), // Border thickness
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val qrPath = customQrImagePath
                            if (qrPath != null && File(qrPath).exists()) {
                                AsyncImage(
                                    model = File(qrPath),
                                    contentDescription = "Author Custom QR Scanner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                AsyncImage(
                                    model = com.example.R.drawable.author_paytm_qr,
                                    contentDescription = "Author Official Paytm QR Scanner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    // Scanner Image Selection Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (customQrImagePath != null) "Change Scanner" else "Upload My Scanner", fontSize = 12.sp)
                        }

                        if (customQrImagePath != null) {
                            OutlinedButton(
                                onClick = {
                                    customQrImagePath = null
                                    prefs.edit().remove("author_qr_path").apply()
                                    Toast.makeText(context, "Default UPI QR restored", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reset", fontSize = 12.sp)
                            }
                        }
                    }

                    // UPI ID display & copy
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("UPI: $authorUpiId", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { AppUtils.copyToClipboard(context, authorUpiId, "Author UPI ID") },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Quick Contribution Amount
                    Text("Select Contribution Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("50", "100", "200", "500").forEach { amt ->
                            val isSelected = (selectedAmount == amt && customAmount.isBlank())
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedAmount = amt
                                    customAmount = ""
                                },
                                label = { Text("₹$amt", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { customAmount = it },
                        placeholder = { Text("Or enter custom amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                    )

                    // Action buttons
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUriString))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No UPI App found. Scan the QR code with GPay/PhonePe/Paytm!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pay ₹$effectiveAmount via UPI App")
                    }

                    FilledTonalButton(
                        onClick = {
                            val qrPath = customQrImagePath
                            if (qrPath != null && File(qrPath).exists()) {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    File(qrPath)
                                )
                                AppUtils.shareImage(context, uri, "Support OmniTool Developer")
                            } else {
                                val bmp = android.graphics.BitmapFactory.decodeResource(
                                    context.resources,
                                    com.example.R.drawable.author_paytm_qr
                                )
                                if (bmp != null) {
                                    AppUtils.saveAndShareBitmap(context, bmp, "Support OmniTool Developer")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share / Save Scanner QR")
                    }
                }
            }
        }
    }

    // Configure UPI ID Dialog
    if (showEditUpiDialog) {
        var tempUpi by remember { mutableStateOf(authorUpiId) }
        var tempName by remember { mutableStateOf(authorName) }

        AlertDialog(
            onDismissRequest = { showEditUpiDialog = false },
            title = { Text("Configure Developer UPI ID") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your UPI VPA ID to receive funds directly from users:", fontSize = 13.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = tempUpi,
                        onValueChange = { tempUpi = it },
                        label = { Text("UPI ID (e.g. pickprimehelp@okaxis)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Payee Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempUpi.isNotBlank()) {
                            authorUpiId = tempUpi.trim()
                            authorName = tempName.trim()
                            prefs.edit()
                                .putString("author_upi_id", authorUpiId)
                                .putString("author_name", authorName)
                                .apply()
                            showEditUpiDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditUpiDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
