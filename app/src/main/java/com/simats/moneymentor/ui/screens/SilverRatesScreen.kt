package com.simats.moneymentor.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.MoneyMentorTheme
import com.simats.moneymentor.ui.theme.LiveDotColor

// ----- Data Models -----

data class SilverRateData(
    val type: String,
    val purity: String,
    val pricePerGram: String,
    val changePercent: String,
    val isPositive: Boolean,
    val emoji: String
)

// Reusing ChartPeriod from GoldRatesScreen if it was public, 
// but since it's in a package-private way or just to be safe, I'll redefine or use the same.
// Actually, I'll redefine it here to keep it self-contained.
enum class SilverChartPeriod(val label: String) {
    ONE_DAY("1D"), ONE_WEEK("1W"), ONE_MONTH("1M"), THREE_MONTHS("3M"), ONE_YEAR("1Y")
}

// ----- Screen -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilverRatesScreen(
    viewModel: com.simats.moneymentor.ui.viewmodels.SilverViewModel? = null,
    onBack: () -> Unit = {}
) {
    val silverRateState by viewModel?.rateState?.collectAsState() ?: remember { mutableStateOf(com.simats.moneymentor.ui.viewmodels.SilverRateState.Idle) }

    LaunchedEffect(Unit) {
        viewModel?.fetchRates(force = true)
    }

    LaunchedEffect(silverRateState) {
        if (silverRateState is com.simats.moneymentor.ui.viewmodels.SilverRateState.Error) {
            kotlinx.coroutines.delay(2000)
            viewModel?.fetchRates(force = true)
        }
    }

    val silverGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF8E9EAB), Color(0xFF606C88))
    )

    // Derived Data from state
    val silverValues = remember(silverRateState) {
        when (val state = silverRateState) {
            is com.simats.moneymentor.ui.viewmodels.SilverRateState.Success -> {
                val price = "₹ ${state.response.silverPrice}"
                val change = state.response.todayChange ?: "0.00%"
                val history = state.response.table ?: emptyList()
                Triple(price, change, history)
            }
            is com.simats.moneymentor.ui.viewmodels.SilverRateState.Error -> Triple("N/A", "0.00%", emptyList<com.simats.moneymentor.data.model.SilverRateHistory>())
            else -> Triple("", "0.00%", emptyList<com.simats.moneymentor.data.model.SilverRateHistory>())
        }
    }

    val currentPrice = silverValues.first
    val currentChange = silverValues.second
    val historyData = silverValues.third
    val isRatePositive = remember(currentChange) { !currentChange.startsWith("-") }

    val silverData = remember(currentPrice, currentChange, isRatePositive) {
        SilverRateData("Silver", "999 Fine Silver", currentPrice, currentChange, isRatePositive, "🥈")
    }

    val isLoading = silverRateState is com.simats.moneymentor.ui.viewmodels.SilverRateState.Loading || 
                    silverRateState is com.simats.moneymentor.ui.viewmodels.SilverRateState.Idle

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(silverGradient)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Silver Rates",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Live prices & trends",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Status Indicator
            SilverLiveStatusRow()

            // Main Silver Rate Card
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(color = Color(0xFF8E9EAB))
                    }
                }
            } else if (silverRateState is com.simats.moneymentor.ui.viewmodels.SilverRateState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load silver rates", color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel?.fetchRates() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B))
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                MainSilverRateCard(data = silverData)
            }

            // Dynamic History Section
            Card(
                modifier = Modifier.fillMaxWidth().height(750.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                when {
                    isLoading -> {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(color = Color(0xFF8E9EAB))
                        }
                    }
                    silverRateState is com.simats.moneymentor.ui.viewmodels.SilverRateState.Error -> {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Click Retry above to load graph", color = Color.Gray)
                        }
                    }
                    historyData.isEmpty() -> {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("No history data available", color = Color.Gray)
                        }
                    }
                    else -> {
                        SilverHistoryGraph(data = historyData)
                    }
                }
            }

            // Investment Tips & Comparison (Only on Success)
            if (silverRateState is com.simats.moneymentor.ui.viewmodels.SilverRateState.Success) {
                SilverInvestmentTipsCard()
                
                val numericPrice = currentPrice.replace("₹", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
                if (numericPrice > 0) {
                    ComparisonCard(pricePerGram = numericPrice)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SilverLiveStatusRow() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(LiveDotColor.copy(alpha = alpha), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Live prices updating",
            color = Color(0xFF64748B),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MainSilverRateCard(data: SilverRateData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Medal Icon Placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCBD5E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🥈", fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            data.type,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            data.purity,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                
                val trendColor = if (data.isPositive) Color(0xFF059669) else Color(0xFFD50000)
                val trendBg = if (data.isPositive) Color(0xFFD1FAE5) else Color(0xFFFFEBEE)
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = trendBg,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (data.isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            data.changePercent,
                            color = trendColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    data.pricePerGram,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "per gram",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}









@Composable
fun SilverInvestmentTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Silver Investment Tips",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Silver is more volatile than gold but offers higher growth potential. It's widely used in electronics and solar panels, making it a strategic investment for the green energy future.",
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun YearlyStatCard(label: String, value: String, date: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(4.dp))
            Text(date, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun ComparisonCard(pricePerGram: Double) {
    val price10g = pricePerGram * 10
    val price1kg = pricePerGram * 1000
    
    val formatter = java.text.DecimalFormat("#,##,##0.00")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Silver per Kilogram", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Text("₹ ${formatter.format(price1kg)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("10 Grams", fontSize = 12.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(4.dp))
                Text("₹ ${formatter.format(price10g)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            }
        }
    }
}

@Composable
fun SilverHistoryGraph(data: List<com.simats.moneymentor.data.model.SilverRateHistory>) {
    val processedData = remember(data) {
        val prices = data.map { it.price ?: 0.0 }.filter { it > 0 }.reversed()
        if (prices.isEmpty()) return@remember null

        val maxP = prices.maxOrNull() ?: 1.0
        val minP = prices.minOrNull() ?: 0.0
        val r = (maxP - minP).coerceAtLeast(1.0)
        
        val pMin = (minP - (r * 0.1)).coerceAtLeast(0.0)
        val pMax = maxP + (r * 0.1)
        val actRange = (pMax - pMin).coerceAtLeast(1.0)
        
        Quintet(prices, pMin, pMax, actRange, data.reversed())
    }

    if (processedData == null) return
    val prices = processedData.t1
    val paddedMin = processedData.t2
    val paddedMax = processedData.t3
    val actualRange = processedData.t4
    val fullData = processedData.t5

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    val silverStrokeColor = Color(0xFF94A3B8)
    val silverFillGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF94A3B8).copy(alpha = 0.3f), Color.Transparent)
    )

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val paddingStart = 55.dp
            val paddingBottom = 120.dp // Massively increased for labels
            val paddingTop = 10.dp
            val paddingEnd = 40.dp
            
            val graphWidthDp = maxWidth - paddingStart - paddingEnd
            val graphHeightDp = maxHeight - paddingTop - paddingBottom

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = paddingStart, end = paddingEnd, top = paddingTop, bottom = paddingBottom)
                    .pointerInput(prices) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / (prices.size - 1).coerceAtLeast(1)
                            val index = (offset.x / stepX).toInt().coerceIn(0, prices.size - 1)
                            
                            val pointX = index * stepX
                            if (kotlin.math.abs(offset.x - pointX) < stepX) {
                                selectedIndex = index
                            } else {
                                selectedIndex = null
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val stepX = width / (prices.size - 1).coerceAtLeast(1)

                // Draw Grid
                val numYLines = 5
                for (i in 0..numYLines) {
                    val y = height - (i * height / numYLines)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                val path = Path()
                val fillPath = Path()

                prices.forEachIndexed { index, price ->
                    val x = index * stepX
                    val y = height - ((price - paddedMin) / actualRange * height).toFloat()

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevPrice = prices[index - 1]
                        val prevX = (index - 1) * stepX
                        val prevY = height - ((prevPrice - paddedMin) / actualRange * height).toFloat()
                        
                        val controlX1 = prevX + (x - prevX) / 2f
                        val controlX2 = prevX + (x - prevX) / 2f
                        
                        path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                    }
                }

                fillPath.lineTo(width, height)
                fillPath.close()

                // Draw Fill
                drawPath(
                    path = fillPath,
                    brush = silverFillGradient
                )

                // Draw Stroke
                drawPath(
                    path = path,
                    color = silverStrokeColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw Points
                prices.forEachIndexed { index, price ->
                    val x = index * stepX
                    val y = height - ((price - paddedMin) / actualRange * height).toFloat()
                    
                    if (index == selectedIndex) {
                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = silverStrokeColor, radius = 6.dp.toPx(), center = Offset(x, y), style = Stroke(width = 2.dp.toPx()))
                        
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    } else {
                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x, y))
                        drawCircle(color = silverStrokeColor, radius = 4.dp.toPx(), center = Offset(x, y), style = Stroke(width = 1.5.dp.toPx()))
                    }
                }
            }

            // Y-Axis labels
            val numYLines = 5
            for (i in 0..numYLines) {
                val priceVal = paddedMin + (i * actualRange / numYLines)
                val yDp = paddingTop + (graphHeightDp * (1f - i.toFloat() / numYLines))
                Text(
                    text = "${priceVal.toInt()}",
                    fontSize = 12.sp,
                    color = Color(0xFF4B5563),
                    modifier = Modifier
                        .offset(x = 0.dp, y = yDp - 8.dp)
                        .width(paddingStart - 6.dp),
                    textAlign = TextAlign.End
                )
            }

            // X-Axis labels
            val fullData = data.reversed()
            val numXLines = prices.size
            val labelIndices = if (numXLines >= 5) {
                setOf(0, numXLines / 4, numXLines / 2, (3 * numXLines) / 4, numXLines - 1)
            } else {
                (0 until numXLines).toSet()
            }

            for (i in 0 until numXLines) {
                if (i in labelIndices) {
                    val xDp = paddingStart + (graphWidthDp * (i.toFloat() / (numXLines - 1).coerceAtLeast(1)))
                    val dateLabel = fullData.getOrNull(i)?.date?.split("-")?.let { parts ->
                        // Find the part that is likely the day (usually first or last, but definitely not 4 digits which is the year)
                        parts.find { it.length <= 2 } ?: parts.firstOrNull() ?: ""
                    } ?: ""
                    Text(
                        text = dateLabel,
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563),
                        modifier = Modifier
                            .offset(x = xDp - (if (i == numXLines - 1) 36.dp else 15.dp), y = paddingTop + graphHeightDp + 60.dp) // Lowered labels even more
                            .width(if (i == numXLines - 1) 40.dp else 30.dp),
                        textAlign = if (i == numXLines - 1) TextAlign.End else TextAlign.Center
                    )
                }
            }

            // Floating Detail Card
            selectedIndex?.let { index ->
                val price = prices[index]
                val item = fullData.getOrNull(index)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = paddingStart, end = paddingEnd, top = paddingTop, bottom = paddingBottom),
                    contentAlignment = if (index > prices.size / 2) Alignment.TopStart else Alignment.TopEnd
                ) {
                    Card(
                        modifier = Modifier
                            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                            .wrapContentSize(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .defaultMinSize(minWidth = 100.dp)
                        ) {
                            Text(
                                item?.date ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF4B5563)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(silverStrokeColor, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Silver: ₹$price", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SilverRatesScreenPreview() {
    MoneyMentorTheme {
        SilverRatesScreen()
    }
}
// Helper for SilverHistoryGraph
data class Quintet<T1, T2, T3, T4, T5>(val t1: T1, val t2: T2, val t3: T3, val t4: T4, val t5: T5)
