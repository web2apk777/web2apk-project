package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush? = Brush.linearGradient(listOf(TechBluePrimary.copy(alpha = 0.4f), EmeraldAccent.copy(alpha = 0.2f))),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(if (borderBrush != null) Modifier.border(1.dp, borderBrush, RoundedCornerShape(20.dp)) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun StepperHeader(
    currentStep: Int,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf("Source", "App Info", "Branding", "Style", "Settings", "Build")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isCompleted = stepNumber < currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStepClick(stepNumber) }
                    .testTag("step_indicator_$stepNumber")
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive -> TechBluePrimary
                                isCompleted -> EmeraldAccent
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "$stepNumber",
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    maxLines = 1,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) TechBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        "COMPLETE" -> EmeraldAccent.copy(alpha = 0.2f) to EmeraldAccent
        "BUILDING", "PREPARING", "QUEUED", "SIGNING" -> TechBluePrimary.copy(alpha = 0.2f) to TechBlueLight
        "FAILED" -> StatusError.copy(alpha = 0.2f) to StatusError
        else -> Color.Gray.copy(alpha = 0.2f) to Color.Gray
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = bgColor
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ColorPickerRow(
    label: String,
    currentColorHex: String,
    presetColors: List<String> = listOf("#2563EB", "#10B981", "#8B5CF6", "#EF4444", "#F59E0B", "#0F172A", "#FFFFFF"),
    onColorSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(currentColorHex, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            presetColors.forEach { hex ->
                val color = parseHexColor(hex)
                val isSelected = currentColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) TechBluePrimary else Color.Gray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) }
                        .testTag("color_picker_$hex")
                )
            }
        }
    }
}

fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        val parsed = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or parsed)
        } else if (clean.length == 8) {
            Color(parsed)
        } else {
            Color.Blue
        }
    } catch (e: Exception) {
        Color.Blue
    }
}
