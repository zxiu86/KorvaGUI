package com.example.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.animation.BezierHandle
import com.example.engine.animation.InterpolationType
import com.example.engine.animation.KeyframeData
import com.example.engine.animation.TrackData
import com.example.ui.theme.*

@Composable
fun AnimationCurveEditorDialog(
    track: TrackData?,
    keyframe: KeyframeData?,
    onDismiss: () -> Unit,
    onApplyInterpolation: (InterpolationType, BezierHandle, BezierHandle) -> Unit
) {
    var selectedType by remember(keyframe) {
        mutableStateOf(keyframe?.interpolation ?: InterpolationType.EASE_IN_OUT)
    }

    var handleIn by remember(keyframe) {
        mutableStateOf(keyframe?.handleIn ?: BezierHandle(0.25f, 0.1f))
    }
    var handleOut by remember(keyframe) {
        mutableStateOf(keyframe?.handleOut ?: BezierHandle(0.25f, 1.0f))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = EngineSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioPurpleBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, tint = StudioYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("محرر المنحنيات (Curve / Interpolation)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }

                // Interpolation Presets Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineBackground)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    InterpolationType.values().forEach { type ->
                        val isChosen = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isChosen) StudioPurple else Color.Transparent)
                                .clickable {
                                    selectedType = type
                                    when (type) {
                                        InterpolationType.LINEAR -> {
                                            handleIn = BezierHandle(0.0f, 0.0f)
                                            handleOut = BezierHandle(1.0f, 1.0f)
                                        }
                                        InterpolationType.EASE_IN -> {
                                            handleIn = BezierHandle(0.42f, 0.0f)
                                            handleOut = BezierHandle(1.0f, 1.0f)
                                        }
                                        InterpolationType.EASE_OUT -> {
                                            handleIn = BezierHandle(0.0f, 0.0f)
                                            handleOut = BezierHandle(0.58f, 1.0f)
                                        }
                                        InterpolationType.EASE_IN_OUT -> {
                                            handleIn = BezierHandle(0.42f, 0.0f)
                                            handleOut = BezierHandle(0.58f, 1.0f)
                                        }
                                        else -> {}
                                    }
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.label,
                                color = if (isChosen) Color.White else TextSecondary,
                                fontSize = 8.5.sp,
                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Description of chosen curve
                Text(
                    text = selectedType.description,
                    color = StudioPurpleLight,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Interactive Bezier Curve Canvas with Draggable Handles
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(EngineBackground)
                        .border(0.8.dp, StudioBorder, RoundedCornerShape(6.dp))
                        .padding(16.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedType) {
                                if (selectedType == InterpolationType.BEZIER) {
                                    detectDragGestures { change, _ ->
                                        change.consume()
                                        val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                        val normY = (1f - change.position.y / size.height).coerceIn(0f, 1f)

                                        if (normX < 0.5f) {
                                            handleIn = BezierHandle(normX, normY)
                                        } else {
                                            handleOut = BezierHandle(normX, normY)
                                        }
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height

                        // Grid lines
                        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(0f, h * 0.25f), Offset(w, h * 0.25f), 0.5f)
                        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 0.5f)
                        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(0f, h * 0.75f), Offset(w, h * 0.75f), 0.5f)

                        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), 0.5f)
                        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), 0.5f)
                        drawLine(StudioBorder.copy(alpha = 0.3f), Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), 0.5f)

                        // Reference diagonal
                        drawLine(StudioBorder.copy(alpha = 0.2f), Offset(0f, h), Offset(w, 0f), 0.8f)

                        // Start & End Points
                        val p0 = Offset(0f, h)
                        val p3 = Offset(w, 0f)

                        val p1 = Offset(handleIn.x * w, h - handleIn.y * h)
                        val p2 = Offset(handleOut.x * w, h - handleOut.y * h)

                        // Draw Tangent Control Lines
                        if (selectedType == InterpolationType.BEZIER) {
                            drawLine(StudioYellow.copy(alpha = 0.6f), p0, p1, 1.2f)
                            drawLine(StudioYellow.copy(alpha = 0.6f), p3, p2, 1.2f)

                            drawCircle(StudioYellow, radius = 5.dp.toPx(), center = p1)
                            drawCircle(StudioYellow, radius = 5.dp.toPx(), center = p2)
                        }

                        // Curve Path
                        val curvePath = Path().apply {
                            moveTo(p0.x, p0.y)
                            when (selectedType) {
                                InterpolationType.CONSTANT -> {
                                    lineTo(p3.x, p0.y)
                                    lineTo(p3.x, p3.y)
                                }
                                InterpolationType.LINEAR -> {
                                    lineTo(p3.x, p3.y)
                                }
                                else -> {
                                    cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
                                }
                            }
                        }

                        drawPath(
                            path = curvePath,
                            color = StudioPurpleLight,
                            style = Stroke(width = 2.5f)
                        )

                        // Draw Keyframe endpoints
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = p0)
                        drawCircle(Color.White, radius = 4.dp.toPx(), center = p3)
                    }
                }

                // Handle values indicators
                if (selectedType == InterpolationType.BEZIER) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Handle 1: (${String.format("%.2f", handleIn.x)}, ${String.format("%.2f", handleIn.y)})",
                            color = TextMuted,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Handle 2: (${String.format("%.2f", handleOut.x)}, ${String.format("%.2f", handleOut.y)})",
                            color = TextMuted,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Apply & Close Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EngineCardBg),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            onApplyInterpolation(selectedType, handleIn, handleOut)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioPurple),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f).height(32.dp)
                    ) {
                        Text("تطبيق المنحنى", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
