package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KorvaDangerButton
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExitConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "إغلاق التطبيق",
        subtitle = "تأكيد الخروج من korva engine",
        icon = Icons.Default.PowerSettingsNew,
        iconTint = KorvaRed,
        maxWidth = 380.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "البقاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaDangerButton(
                text = "خروج نهائي",
                onClick = onConfirmExit,
                icon = Icons.Default.ExitToApp,
                modifier = Modifier
                    .weight(1f)
                    .testTag("confirm_exit_button")
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(KorvaRed.copy(alpha = 0.10f))
                    .border(0.8.dp, KorvaRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = KorvaRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "هل أنت متأكد من الخروج؟ تأكد من حفظ كل تعديلاتك قبل الإغلاق.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                text = "سيتم إنهاء الجلسة والعودة للشاشة الرئيسية للجهاز.",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
