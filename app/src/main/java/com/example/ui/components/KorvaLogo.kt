package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineSurfaceVariant
import com.example.ui.theme.KorvaCyan
import com.example.ui.theme.KorvaCyanDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KorvaLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Row(
        modifier = modifier.testTag("app_logo_header"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo Container with glowing border
        Box(
            modifier = Modifier
                .size(if (compact) 44.dp else 52.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp), ambientColor = KorvaCyan, spotColor = KorvaCyan)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(EngineSurfaceVariant, Color(0xFF0F172A))
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(KorvaCyan, Color(0xFF1E3A8A))
                    ),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_korva_logo),
                contentDescription = "Korva Engine Logo",
                modifier = Modifier
                    .size(if (compact) 36.dp else 44.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "korva",
                    color = TextPrimary,
                    fontSize = if (compact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "engine",
                    color = KorvaCyan,
                    fontSize = if (compact) 18.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Engine Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(KorvaCyan.copy(alpha = 0.15f))
                        .border(0.8.dp, KorvaCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PRO v2.4",
                        color = KorvaCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "بيئة التطوير والمحرك الرسومي المتقدم",
                color = TextSecondary,
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
