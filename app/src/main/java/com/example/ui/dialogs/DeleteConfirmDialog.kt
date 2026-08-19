package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.components.KorvaDangerButton
import com.example.ui.components.KorvaDialog
import com.example.ui.components.KorvaOutlinedButton
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.KorvaRed
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeleteConfirmDialog(
    project: ProjectEntity,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    KorvaDialog(
        onDismissRequest = onDismiss,
        title = "حذف المشروع",
        subtitle = "إجراء لا يمكن التراجع عنه",
        icon = Icons.Default.DeleteForever,
        iconTint = KorvaRed,
        maxWidth = 400.dp,
        buttons = {
            KorvaOutlinedButton(
                text = "إلغاء",
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            )

            KorvaDangerButton(
                text = "حذف نهائي",
                onClick = onConfirmDelete,
                icon = Icons.Default.DeleteForever,
                modifier = Modifier
                    .weight(1f)
                    .testTag("confirm_delete_button")
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Project card preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EngineCardBg)
                    .border(0.8.dp, KorvaRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = project.name,
                            color = KorvaRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = project.templateType,
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = project.path,
                        color = TextMuted,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "⚠️ تحذير: سيتم حذف جميع المشاهد والكائنات والموارد الخاصة بهذا المشروع من الذاكرة المحلية بشكل نهائي.",
                color = TextSecondary,
                fontSize = 10.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}
