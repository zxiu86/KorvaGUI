package com.example.ui.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.engine.animation.BezierHandle
import com.example.engine.animation.InterpolationType
import com.example.engine.animation.KeyframeData
import com.example.engine.animation.TrackData
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.components.KorvaPrimaryButton
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.KorvaPurple
import com.example.ui.theme.KorvaPurpleLight
import com.example.ui.theme.KorvaYellow
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

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

    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "محرر المنحنيات (Curve / Interpolation)",
        subtitle = "${track?.name ?: "المسار"} • نمط الحركة وتوزيع السرعات",
        icon = Icons.Default.ShowChart,
        iconTint = KorvaYellow,
        maxWidth = 440.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaPrimaryButton(
                text = "تطبيق المنحنى",
                onClick = {
                    onApplyInterpolation(selectedType, handleIn, handleOut)
                    onDismiss()
                },
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1.2f)
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Interpolation Presets Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineBackground)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                InterpolationType.values().forEach { type ->
                    val isChosen = selectedType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isChosen) KorvaPurple else Color.Transparent)
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
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.label,
                            color = if (isChosen) Color.White else TextSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Description of chosen curve
            Text(
                text = selectedType.description,
                color = KorvaPurpleLight,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Interactive Bezier Curve Canvas with Draggable Handles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineBackground)
                    .border(0.8.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(14.dp)
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
                        drawLine(KorvaYellow.copy(alpha = 0.6f), p0, p1, 1.2f)
                        drawLine(KorvaYellow.copy(alpha = 0.6f), p3, p2, 1.2f)

                        drawCircle(KorvaYellow, radius = 5.dp.toPx(), center = p1)
                        drawCircle(KorvaYellow, radius = 5.dp.toPx(), center = p2)
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
                        color = KorvaPurpleLight,
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
                        text = "Handle 1: (${String.format(java.util.Locale.US, "%.2f", handleIn.x)}, ${String.format(java.util.Locale.US, "%.2f", handleIn.y)})",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Handle 2: (${String.format(java.util.Locale.US, "%.2f", handleOut.x)}, ${String.format(java.util.Locale.US, "%.2f", handleOut.y)})",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
