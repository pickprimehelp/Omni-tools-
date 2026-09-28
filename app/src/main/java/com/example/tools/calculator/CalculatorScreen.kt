package com.example.tools.calculator

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

data class CurrencyRate(val code: String, val name: String, val symbol: String, val rateToUsd: Double)

val CURRENCIES = listOf(
    CurrencyRate("INR", "Indian Rupee", "₹", 86.8),
    CurrencyRate("USD", "US Dollar", "$", 1.0),
    CurrencyRate("EUR", "Euro", "€", 0.92),
    CurrencyRate("GBP", "British Pound", "£", 0.79),
    CurrencyRate("AED", "UAE Dirham", "د.إ", 3.67),
    CurrencyRate("SAR", "Saudi Riyal", "﷼", 3.75),
    CurrencyRate("JPY", "Japanese Yen", "¥", 152.0),
    CurrencyRate("CAD", "Canadian Dollar", "C$", 1.38),
    CurrencyRate("AUD", "Australian Dollar", "A$", 1.54),
    CurrencyRate("SGD", "Singapore Dollar", "S$", 1.34)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var mode by remember { mutableIntStateOf(0) } // 0: Scientific Calculator, 1: Currency Converter

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator & Currency", fontWeight = FontWeight.Bold) },
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
            TabRow(selectedTabIndex = mode) {
                Tab(
                    selected = mode == 0,
                    onClick = { mode = 0 },
                    text = { Text("Scientific Calc") },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) }
                )
                Tab(
                    selected = mode == 1,
                    onClick = { mode = 1 },
                    text = { Text("Currency Exchange") },
                    icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) }
                )
            }

            when (mode) {
                0 -> ScientificCalcView()
                1 -> CurrencyConverterView()
            }
        }
    }
}

@Composable
fun ScientificCalcView() {
    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }
    var isScientificExpanded by remember { mutableStateOf(false) }

    fun evaluate(expr: String): String {
        return try {
            if (expr.isBlank()) return ""
            var clean = expr.replace("×", "*").replace("÷", "/").replace("π", "${Math.PI}").replace("e", "${Math.E}")
            // Handle square root
            clean = clean.replace("√(\\d+(\\.\\d+)?)".toRegex()) {
                val num = it.groupValues[1].toDouble()
                sqrt(num).toString()
            }
            // Evaluate basic arithmetic
            val res = evalMath(clean)
            if (res.isNaN() || res.isInfinite()) "Error"
            else if (res == res.toLong().toDouble()) res.toLong().toString()
            else String.format("%.6f", res).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            "Error"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Screen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isScientificExpanded,
                        onClick = { isScientificExpanded = !isScientificExpanded },
                        label = { Text(if (isScientificExpanded) "Scientific (Expanded)" else "Standard (Compact)", fontSize = 11.sp) }
                    )
                    IconButton(onClick = {
                        if (expression.isNotEmpty()) {
                            expression = expression.dropLast(1)
                            resultText = evaluate(expression)
                        }
                    }) {
                        Icon(Icons.Default.Backspace, contentDescription = "Backspace")
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (expression.isEmpty()) "0" else expression,
                        fontSize = if (expression.length > 15) 24.sp else 34.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 2
                    )
                    if (resultText.isNotEmpty() && resultText != expression) {
                        Text(
                            text = "= $resultText",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Buttons Grid
        val basicButtons = listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "%", "=")
        )

        val scientificButtons = listOf(
            listOf("sin", "cos", "tan", "log"),
            listOf("ln", "√", "^", "π")
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isScientificExpanded) {
                scientificButtons.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { btn ->
                            CalcButton(
                                text = btn,
                                modifier = Modifier.weight(1f),
                                isAccent = true
                            ) {
                                when (btn) {
                                    "sin", "cos", "tan", "log", "ln" -> {
                                        try {
                                            val currentVal = evaluate(expression).toDoubleOrNull() ?: 0.0
                                            val res = when (btn) {
                                                "sin" -> sin(Math.toRadians(currentVal))
                                                "cos" -> cos(Math.toRadians(currentVal))
                                                "tan" -> tan(Math.toRadians(currentVal))
                                                "log" -> log10(currentVal)
                                                else -> ln(currentVal)
                                            }
                                            expression = String.format("%.4f", res)
                                            resultText = expression
                                        } catch (_: Exception) {}
                                    }
                                    "√" -> expression += "√"
                                    "π" -> expression += "π"
                                    "^" -> expression += "^"
                                }
                            }
                        }
                    }
                }
            }

            basicButtons.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { btn ->
                        val isOp = btn in listOf("÷", "×", "-", "+", "=")
                        val isClear = btn == "C"

                        CalcButton(
                            text = btn,
                            modifier = Modifier.weight(1f),
                            isPrimary = btn == "=",
                            isOp = isOp,
                            isClear = isClear
                        ) {
                            when (btn) {
                                "C" -> {
                                    expression = ""
                                    resultText = ""
                                }
                                "=" -> {
                                    val finalRes = evaluate(expression)
                                    if (finalRes.isNotEmpty() && finalRes != "Error") {
                                        expression = finalRes
                                        resultText = ""
                                    }
                                }
                                "%" -> {
                                    val num = evaluate(expression).toDoubleOrNull()
                                    if (num != null) {
                                        expression = (num / 100.0).toString()
                                        resultText = expression
                                    }
                                }
                                else -> {
                                    expression += btn
                                    resultText = evaluate(expression)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    isOp: Boolean = false,
    isClear: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when {
        isPrimary -> MaterialTheme.colorScheme.primary
        isClear -> MaterialTheme.colorScheme.errorContainer
        isOp -> MaterialTheme.colorScheme.secondaryContainer
        isAccent -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        isClear -> MaterialTheme.colorScheme.onErrorContainer
        isOp -> MaterialTheme.colorScheme.onSecondaryContainer
        isAccent -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CurrencyConverterView() {
    var amountInput by remember { mutableStateOf("100") }
    var fromCurrency by remember { mutableStateOf(CURRENCIES[1]) } // USD
    var toCurrency by remember { mutableStateOf(CURRENCIES[0]) } // INR

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    // Rate conversion: amount in USD = amount / from.rateToUsd
    // converted = (amount in USD) * to.rateToUsd
    val convertedAmount = if (fromCurrency.rateToUsd > 0) {
        (amount / fromCurrency.rateToUsd) * toCurrency.rateToUsd
    } else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Converted Amount", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                Text(
                    "${toCurrency.symbol} ${String.format(java.util.Locale.getDefault(), "%,.2f", convertedAmount)}",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "1 ${fromCurrency.code} = ${String.format(java.util.Locale.getDefault(), "%.4f", toCurrency.rateToUsd / fromCurrency.rateToUsd)} ${toCurrency.code}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Amount Input Field
        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Enter Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Text(fromCurrency.symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
        )

        // Swap Currencies
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalIconButton(
                onClick = {
                    val temp = fromCurrency
                    fromCurrency = toCurrency
                    toCurrency = temp
                }
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap Currencies")
            }
        }

        // From Currency Selector
        Text("From Currency (${fromCurrency.code})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CURRENCIES.take(5).forEach { curr ->
                FilterChip(
                    selected = fromCurrency == curr,
                    onClick = { fromCurrency = curr },
                    label = { Text("${curr.symbol} ${curr.code}", fontSize = 11.sp) }
                )
            }
        }

        // To Currency Selector
        Text("To Currency (${toCurrency.code})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CURRENCIES.take(5).forEach { curr ->
                FilterChip(
                    selected = toCurrency == curr,
                    onClick = { toCurrency = curr },
                    label = { Text("${curr.symbol} ${curr.code}", fontSize = 11.sp) }
                )
            }
        }
    }
}

// Simple recursive descent parser for basic math expressions (+, -, *, /)
fun evalMath(str: String): Double {
    var pos = -1
    var ch = -1

    fun nextChar() {
        ch = if (++pos < str.length) str[pos].code else -1
    }

    fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parseExpression(): Double {
        var x = 0.0
        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var fact: Double
            val startPos = pos
            if (eat('('.code)) {
                fact = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                fact = str.substring(startPos, pos).toDouble()
            } else {
                return 0.0
            }

            if (eat('^'.code)) fact = fact.pow(parseFactor())
            return fact
        }

        fun parseTerm(): Double {
            var term = parseFactor()
            while (true) {
                if (eat('*'.code)) term *= parseFactor()
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor != 0.0) term /= divisor else return Double.NaN
                } else return term
            }
        }

        x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm()
            else if (eat('-'.code)) x -= parseTerm()
            else return x
        }
    }

    nextChar()
    return parseExpression()
}
