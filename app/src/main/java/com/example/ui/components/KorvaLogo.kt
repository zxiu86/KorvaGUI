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
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurfaceVariant
import com.example.ui.theme.EngineWhiteBorder
import com.example.ui.theme.EngineWhiteGlass
import com.example.ui.theme.EngineWhiteMuted
import com.example.ui.theme.EngineWhitePrimary
import com.example.ui.theme.EngineWhiteTranslucent
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
        // Logo Container with frosted glass border
        Box(
            modifier = Modifier
                .size(if (compact) 40.dp else 48.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp), ambientColor = Color.Black, spotColor = Color.Black)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(EngineSurfaceVariant, EngineCardBg)
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(EngineWhiteBorder, Color(0x10FFFFFF))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_korva_logo),
                contentDescription = "Korva Engine Logo",
                modifier = Modifier
                    .size(if (compact) 32.dp else 38.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "korva",
                    color = TextPrimary,
                    fontSize = if (compact) 16.sp else 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "engine",
                    color = EngineWhiteTranslucent,
                    fontSize = if (compact) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Translucent Engine Version Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(EngineWhiteGlass)
                        .border(0.8.dp, EngineWhiteBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "v2.4",
                        color = EngineWhiteMuted,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "بيئة تطوير ومحرك ألعاب",
                color = TextSecondary,
                fontSize = if (compact) 10.5.sp else 11.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
