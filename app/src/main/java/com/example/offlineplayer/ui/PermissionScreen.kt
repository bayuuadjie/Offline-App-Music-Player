package com.example.offlineplayer.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.ui.theme.AppGradient

@Composable
fun PermissionScreen(onGranted: () -> Unit) {
    val context = LocalContext.current
    var denied by remember { mutableStateOf(false) }

    val audioPerm = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_AUDIO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[audioPerm] == true) onGranted() else denied = true
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3B2278), MaterialTheme.colorScheme.background),
                    startY = 0f, endY = 1600f
                )
            )
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier.size(112.dp).clip(RoundedCornerShape(32.dp)).background(AppGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote, contentDescription = null,
                    modifier = Modifier.size(56.dp), tint = Color.White
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Musikmu, tanpa ribet",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Aplikasi ini hanya membaca file musik di perangkatmu. Tanpa internet, tanpa login, tanpa pelacakan.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    val perms = if (Build.VERSION.SDK_INT >= 33)
                        arrayOf(audioPerm, Manifest.permission.POST_NOTIFICATIONS)
                    else
                        arrayOf(audioPerm)
                    launcher.launch(perms)
                },
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) { Text("Izinkan akses musik", style = MaterialTheme.typography.titleMedium) }

            if (denied) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Izin ditolak. Aktifkan izin Musik dan audio di pengaturan aplikasi.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }) { Text("Buka pengaturan") }
            }
        }
    }
}
