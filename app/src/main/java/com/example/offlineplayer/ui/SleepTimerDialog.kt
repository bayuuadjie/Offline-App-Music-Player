package com.example.offlineplayer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
fun SleepTimerDialog(vm: PlayerViewModel, onDismiss: () -> Unit) {
    var custom by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timer tidur") },
        text = {
            Column {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    Text(
                        "$minutes menit",
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.startSleepTimer(minutes); onDismiss() }
                            .padding(vertical = 12.dp)
                    )
                }
                Text(
                    "Setelah lagu ini selesai",
                    Modifier
                        .fillMaxWidth()
                        .clickable { vm.sleepAfterCurrentSong(); onDismiss() }
                        .padding(vertical = 12.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { custom = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Menit kustom") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        val m = custom.toIntOrNull()
                        if (m != null && m > 0) {
                            vm.startSleepTimer(m)
                            onDismiss()
                        }
                    }) { Text("Mulai") }
                }
                if (vm.sleepEndAt > 0 || vm.sleepAfterSong) {
                    TextButton(onClick = { vm.cancelSleep(); onDismiss() }) {
                        Text("Batalkan timer")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}
