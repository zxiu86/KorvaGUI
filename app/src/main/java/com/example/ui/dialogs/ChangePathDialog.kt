package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EngineBackground
import com.example.ui.theme.EngineBorder
import com.example.ui.theme.EngineCardBg
import com.example.ui.theme.EngineSurface
import com.example.ui.theme.KorvaCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChangePathDialog(
    currentPath: String,
    onDismiss: () -> Unit,
    onConfirm: (newPath: String) -> Unit
) {
    var pathInput by remember { mutableStateOf(currentPath) }

    val presetLocations = remember {
        listOf(
            "/storage/emulated/0/KorvaEngine/Projects",
            "/storage/emulated/0/Documents/KorvaProjects",
            "/storage/emulated/0/Download/KorvaGames"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .widthIn(min = 480.dp, max = 600.dp)
                .padding(16.dp)
                .shadow(20.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(EngineSurface)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(KorvaCyan.copy(alpha = 0.5f), EngineBorder)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            color = EngineSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KorvaCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DriveFileMove,
                                contentDescription = null,
                                tint = KorvaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تحديد مسار الحفظ الافتراضي",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = pathInput,
                    onValueChange = { pathInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_path_input"),
                    label = { Text("المسار", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KorvaCyan,
                        unfocusedBorderColor = EngineBorder,
                        focusedContainerColor = EngineCardBg,
                        unfocusedContainerColor = EngineCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "مسارات مقترحة وسريعة:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                presetLocations.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EngineCardBg)
                            .border(0.8.dp, EngineBorder, RoundedCornerShape(6.dp))
                            .clickable { pathInput = preset }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = KorvaCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(preset, color = TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("إلغاء", color = TextSecondary, fontSize = 12.5.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onConfirm(pathInput.ifBlank { currentPath }) },
                        colors = ButtonDefaults.buttonColors(containerColor = KorvaCyan, contentColor = EngineBackground),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_change_path_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ المسار", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
