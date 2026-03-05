package com.simats.moneymentor.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.MoneyMentorTheme
import com.simats.moneymentor.ui.theme.LiveDotColor
import kotlin.math.max
import kotlin.math.min

// ----- Data Models -----

data class GoldRateData(
    val karat: String,
    val purity: String,
    val pricePerGram: String,
    val changePercent: String,
    val isPositive: Boolean,
    val emoji: String
)



// ----- Screen -----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldRatesScreen(
    viewModel: com.simats.moneymentor.ui.viewmodels.GoldViewModel? = null,
    onBack: () -> Unit = {}
) {
    val goldenGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE6A817), Color(0xFFF5C518), Color(0xFFF0A500))
    )

    val rateState by viewModel?.rateState?.collectAsState(initial = com.simats.moneymentor.ui.viewmodels.GoldRateState.Idle) ?: remember { mutableStateOf(com.simats.moneymentor.ui.viewmodels.GoldRateState.Idle) }

    // Fallbacks - Set to empty strings to avoid showing stale/fixed data
    var price24k = ""
    var price22k = ""
    var change24k = ""
    var isPositive24k = true
    var change22k = ""
    var isPositive22k = true

    val isLoading = rateState is com.simats.moneymentor.ui.viewmodels.GoldRateState.Loading || 
                    rateState is com.simats.moneymentor.ui.viewmodels.GoldRateState.Idle

    if (rateState is com.simats.moneymentor.ui.viewmodels.GoldRateState.Success) {
        val response = (rateState as com.simats.moneymentor.ui.viewmodels.GoldRateState.Success).response
        
        // Remove commas and cast to number for calculation, falling back to string manipulation if structure changes
        val purePrice = try {
            val rawPrice = if (response.goldPrice.contains("₹")) response.goldPrice.replace("₹", "") else response.goldPrice
            val numPrice = rawPrice.replace(",", "").trim().toDouble()
            // 22K is typically ~91.6% of 24K price
            val k22Price = numPrice * 0.916
            
            // Format back to string with grouping
            val format = java.text.NumberFormat.getIntegerInstance(java.util.Locale("en", "IN"))
            price24k = "₹" + format.format(numPrice)
            price22k = "₹" + format.format(k22Price.toLong())
        } catch (e: Exception) {
            price24k = "₹${response.goldPrice.replace("₹", "")}"
            price22k = "₹${response.goldPrice.replace("₹", "")}"
        }

        val parseChange = { changeStr: String? ->
            if (changeStr.isNullOrEmpty()) {
                Pair("+0.00", true)
            } else {
                val cleanStr = changeStr.replace("%", "").trim()
                val isPos = !cleanStr.startsWith("-")
                val prefix = if (isPos && !cleanStr.startsWith("+")) "+" else ""
                Pair("$prefix$cleanStr", isPos)
            }
        }
        
        val parsed24k = parseChange(response.change24kToday)
        change24k = parsed24k.first
        isPositive24k = parsed24k.second
        
        val parsed22k = parseChange(response.change22kToday)
        change22k = parsed22k.first
        isPositive22k = parsed22k.second
    }

    val gold24k = GoldRateData("24 KARAT", "99.9% Pure", price24k, change24k, isPositive24k, "🏅")
    val gold22k = GoldRateData("22 KARAT", "91.6% Pure", price22k, change22k, isPositive22k, "🥇")



    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(goldenGradient)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.25f), CircleShape)
                    ) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Gold Rates",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            "Live prices & trends",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFFAFAF5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Indicator
            LiveStatusRow()

            // Gold Rate Cards
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBE6))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(color = Color(0xFFE6A817))
                    }
                }
            } else if (rateState is com.simats.moneymentor.ui.viewmodels.GoldRateState.Error) {
                 Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Unable to load gold rates", color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GoldRateCard(data = gold24k, modifier = Modifier.weight(1f))
                    GoldRateCard(data = gold22k, modifier = Modifier.weight(1f))
                }
            }

            if (rateState is com.simats.moneymentor.ui.viewmodels.GoldRateState.Success) {
                val response = (rateState as com.simats.moneymentor.ui.viewmodels.GoldRateState.Success).response
                GoldHistoryGraph(historyData = response.table)
            }

            // Did You Know
            DidYouKnowCard()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LiveStatusRow() {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
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
                .size(10.dp)
                .background(LiveDotColor.copy(alpha = alpha), CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Live prices updating",
            color = Color(0xFF374151),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GoldRateCard(data: GoldRateData, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBE6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(data.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        data.karat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        data.purity,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            Text(
                data.pricePerGram,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color(0xFF111827)
            )
            Text(
                "per gram",
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
            val isPos = data.isPositive
            val arrow = if (isPos) "↗" else "↘"
            val textColor = if (isPos) Color(0xFF0D9488) else Color(0xFFD50000)
            val bgColor = if (isPos) Color(0xFFCCFBF1) else Color(0xFFFFEBEE)

            Box(
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "$arrow ${data.changePercent}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}


@Composable
fun GoldHistoryGraph(historyData: List<com.simats.moneymentor.data.model.GoldRateHistory>?) {
    if (historyData.isNullOrEmpty()) return

    // Reverse data to have chronological order (oldest left, newest right)
    val data = historyData.reversed()
    if (data.size < 2) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    val max24k = data.maxOfOrNull { it.price24k.toDoubleOrNull() ?: 0.0 } ?: 1.0
    val min24k = data.minOfOrNull { it.price24k.toDoubleOrNull() ?: 0.0 } ?: 0.0
    val max22k = data.maxOfOrNull { it.price22k.toDoubleOrNull() ?: 0.0 } ?: 1.0
    val min22k = data.minOfOrNull { it.price22k.toDoubleOrNull() ?: 0.0 } ?: 0.0

    val overallMax = max(max24k, max22k)
    val overallMin = min(min24k, min22k)
    // Add some padding to min/max
    val range = overallMax - overallMin
    val paddedMax = overallMax + range * 0.1
    val paddedMin = overallMin - range * 0.1
    val actualRange = if (paddedMax > paddedMin) paddedMax - paddedMin else 1.0

    val goldenGradient24k = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color(0xFFFFD700).copy(alpha = 0.1f), Color.Transparent)
    )
    val goldenStroke24k = Color(0xFFF5C518)
    
    val silverGradient22k = Brush.verticalGradient(
        colors = listOf(Color(0xFFE6A817).copy(alpha = 0.4f), Color(0xFFE6A817).copy(alpha = 0.1f), Color.Transparent)
    )
    val goldenStroke22k = Color(0xFFB8860B) // Darker gold for 22k

    Card(
        modifier = Modifier.fillMaxWidth().height(550.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Price Trend (10 Days)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1F2937)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(goldenStroke24k, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("24K", fontSize = 11.sp, color = Color(0xFF6B7280))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(goldenStroke22k, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("22K", fontSize = 11.sp, color = Color(0xFF6B7280))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val paddingStart = 55.dp
                val paddingBottom = 75.dp // Significantly increased padding
                val paddingTop = 10.dp
                val paddingEnd = 16.dp
                
                val graphWidthDp = maxWidth - paddingStart - paddingEnd
                val graphHeightDp = maxHeight - paddingTop - paddingBottom

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = paddingStart, end = paddingEnd, top = paddingTop, bottom = paddingBottom)
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val stepX = width / (data.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).toInt().coerceIn(0, data.size - 1)
                                
                                // Check if touch is close enough to the point X
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
                    val stepX = width / (data.size - 1).coerceAtLeast(1)

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
                    for (i in 0 until data.size) {
                        val x = i * stepX
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1f
                        )
                    }

                    val path24k = Path()
                    val fillPath24k = Path()
                    
                    val path22k = Path()
                    val fillPath22k = Path()

                    data.forEachIndexed { index, item ->
                        val price24 = item.price24k.toDoubleOrNull() ?: 0.0
                        val price22 = item.price22k.toDoubleOrNull() ?: 0.0
                        
                        val x = index * stepX
                        val y24 = height - ((price24 - paddedMin) / actualRange * height).toFloat()
                        val y22 = height - ((price22 - paddedMin) / actualRange * height).toFloat()

                        if (index == 0) {
                            path24k.moveTo(x, y24)
                            fillPath24k.moveTo(x, height)
                            fillPath24k.lineTo(x, y24)
                            
                            path22k.moveTo(x, y22)
                            fillPath22k.moveTo(x, height)
                            fillPath22k.lineTo(x, y22)
                        } else {
                            val prevPrice24 = data[index - 1].price24k.toDoubleOrNull() ?: 0.0
                            val prevPrice22 = data[index - 1].price22k.toDoubleOrNull() ?: 0.0
                            val prevX = (index - 1) * stepX
                            val prevY24 = height - ((prevPrice24 - paddedMin) / actualRange * height).toFloat()
                            val prevY22 = height - ((prevPrice22 - paddedMin) / actualRange * height).toFloat()
                            
                            // Cubic bezier for smooth curves
                            val controlX1 = prevX + (x - prevX) / 2f
                            val controlX2 = prevX + (x - prevX) / 2f
                            
                            path24k.cubicTo(controlX1, prevY24, controlX2, y24, x, y24)
                            fillPath24k.cubicTo(controlX1, prevY24, controlX2, y24, x, y24)
                            
                            path22k.cubicTo(controlX1, prevY22, controlX2, y22, x, y22)
                            fillPath22k.cubicTo(controlX1, prevY22, controlX2, y22, x, y22)
                        }
                    }

                    fillPath24k.lineTo(width, height)
                    fillPath24k.close()
                    
                    fillPath22k.lineTo(width, height)
                    fillPath22k.close()

                    // Draw Fills
                    drawPath(
                        path = fillPath24k,
                        brush = goldenGradient24k
                    )
                    drawPath(
                        path = fillPath22k,
                        brush = silverGradient22k
                    )

                    // Draw Strokes
                    drawPath(
                        path = path24k,
                        color = goldenStroke24k,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    drawPath(
                        path = path22k,
                        color = goldenStroke22k,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw Points
                    data.forEachIndexed { index, item ->
                         val price24 = item.price24k.toDoubleOrNull() ?: 0.0
                         val price22 = item.price22k.toDoubleOrNull() ?: 0.0
                         val x = index * stepX
                         val y24 = height - ((price24 - paddedMin) / actualRange * height).toFloat()
                         val y22 = height - ((price22 - paddedMin) / actualRange * height).toFloat()
                         
                         if (index == selectedIndex) {
                             drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x, y24))
                             drawCircle(color = goldenStroke24k, radius = 6.dp.toPx(), center = Offset(x, y24), style = Stroke(width = 2.dp.toPx()))
                             
                             drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x, y22))
                             drawCircle(color = goldenStroke22k, radius = 6.dp.toPx(), center = Offset(x, y22), style = Stroke(width = 2.dp.toPx()))
                             
                             // Draw a vertical line indicator
                             drawLine(
                                 color = Color.Gray.copy(alpha = 0.5f),
                                 start = Offset(x, 0f),
                                 end = Offset(x, height),
                                 strokeWidth = 1.dp.toPx(),
                                 pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                             )
                         } else {
                             drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x, y24))
                             drawCircle(color = goldenStroke24k, radius = 4.dp.toPx(), center = Offset(x, y24), style = Stroke(width = 1.5.dp.toPx()))
                             
                             drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(x, y22))
                             drawCircle(color = goldenStroke22k, radius = 4.dp.toPx(), center = Offset(x, y22), style = Stroke(width = 1.5.dp.toPx()))
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
                val numXLines = data.size
                for (i in 0 until numXLines) {
                    val showLabel = if (data.size > 7) {
                        i % 2 == 0 || i == data.size - 1
                    } else {
                        true
                    }
                    if (showLabel) {
                        val xDp = paddingStart + (graphWidthDp * (i.toFloat() / (data.size - 1).coerceAtLeast(1)))
                        Text(
                            text = data[i].date.split("-").firstOrNull() ?: data[i].date.take(2),
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563),
                            modifier = Modifier
                            .offset(x = xDp - 15.dp, y = paddingTop + graphHeightDp + 25.dp) // Lowered labels more
                            .width(30.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Floating Card overlay
                selectedIndex?.let { index ->
                    val item = data[index]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = paddingStart, end = paddingEnd, top = paddingTop, bottom = paddingBottom),
                        contentAlignment = if (index > data.size / 2) Alignment.TopStart else Alignment.TopEnd
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
                                    item.date,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF4B5563)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(goldenStroke24k, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("24K: ₹${item.price24k}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(goldenStroke22k, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("22K: ₹${item.price22k}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
fun DidYouKnowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBE6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Did you know?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1F2937)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "24K gold is 99.9% pure gold, ideal for investment. " +
                "22K gold contains 91.6% gold mixed with other metals, making it more durable for jewelry.",
                fontSize = 13.sp,
                color = Color(0xFF4B5563),
                lineHeight = 20.sp
            )
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GoldRatesScreenPreview() {
    MoneyMentorTheme {
        GoldRatesScreen()
    }
}
