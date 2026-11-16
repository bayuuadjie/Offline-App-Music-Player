package com.example.offlineplayer.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
private fun OptionRow(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    )
}

@Composable
fun SleepTimerDialog(vm: PlayerViewModel, onDismiss: () -> Unit) {
    var custom by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timer tidur") },
        text = {
            Column {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    OptionRow("$minutes menit") { vm.startSleepTimer(minutes); onDismiss() }
                }
                OptionRow("Setelah lagu ini selesai") { vm.sleepAfterCurrentSong(); onDismiss() }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { custom = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Menit kustom") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        val m = custom.toIntOrNull()
                        if (m != null && m > 0) {
                            vm.startSleepTimer(m)
                            onDismiss()
                        }
                    }) { Text("Mulai") }
                }
                if (vm.sleepEndAt > 0 || vm.sleepAfterSong) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { vm.cancelSleep(); onDismiss() }) {
                        Text("Batalkan timer")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}
