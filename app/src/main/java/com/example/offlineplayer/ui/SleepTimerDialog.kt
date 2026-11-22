package com.example.offlineplayer.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.TextDim
import com.example.offlineplayer.ui.theme.TextMuted
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
private fun OptionRow(text: String, isSelected: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(shape)
            .background(if (isSelected) Color(0x2B1ED760) else Color(0x14FFFFFF))
            .border(
                1.dp,
                if (isSelected) LiquidGreen.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isSelected) Icons.Filled.Bedtime else Icons.Filled.MusicNote,
            contentDescription = null,
            tint = if (isSelected) LiquidGreen else TextMuted,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) LiquidGreen else Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun SleepTimerDialog(vm: PlayerViewModel, onDismiss: () -> Unit) {
    var custom by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xEB131B15),
        modifier = Modifier.border(
            1.dp,
            Brush.linearGradient(
                listOf(Color.White.copy(alpha = 0.25f), LiquidGreen.copy(alpha = 0.2f))
            ),
            RoundedCornerShape(28.dp)
        ),
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = LiquidGreen, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Timer Tidur",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    OptionRow("$minutes Menit") {
                        vm.startSleepTimer(minutes)
                        onDismiss()
                    }
                }
                OptionRow("Setelah lagu ini selesai", isSelected = vm.sleepAfterSong) {
                    vm.sleepAfterCurrentSong()
                    onDismiss()
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { custom = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Menit kustom", color = TextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = LiquidGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color(0x14FFFFFF),
                            unfocusedContainerColor = Color(0x0AFFFFFF)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val m = custom.toIntOrNull()
                            if (m != null && m > 0) {
                                vm.startSleepTimer(m)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LiquidGreen,
                            contentColor = Color(0xFF031408)
                        )
                    ) {
                        Text("Mulai", fontWeight = FontWeight.Bold)
                    }
                }

                if (vm.sleepEndAt > 0 || vm.sleepAfterSong) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { vm.cancelSleep(); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batalkan Timer", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = TextDim)
            }
        }
    )
}
