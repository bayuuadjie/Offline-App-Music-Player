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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.offlineplayer.ui.theme.DarkScreenGradient
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.TextDim

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
            .background(DarkScreenGradient)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glass Logo Tile
            Box(
                Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0x33FFFFFF),
                                Color(0x1F1ED760),
                                Color(0x0FFFFFFF)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(Color.White.copy(alpha = 0.4f), LiquidGreen.copy(alpha = 0.3f))
                        ),
                        RoundedCornerShape(32.dp)
                    )
                    .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = LiquidGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = LiquidGreen
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Musikmu, Kapan Saja",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Pemutar musik offline bertema Liquid Glass. Tanpa internet, tanpa iklan, dan tanpa pelacakan data.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = TextDim
            )

            Spacer(Modifier.height(36.dp))

            Button(
                onClick = {
                    val perms = if (Build.VERSION.SDK_INT >= 33)
                        arrayOf(audioPerm, Manifest.permission.POST_NOTIFICATIONS)
                    else
                        arrayOf(audioPerm)
                    launcher.launch(perms)
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidGreen,
                    contentColor = Color(0xFF031408)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    "Izinkan Akses Musik",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (denied) {
                Spacer(Modifier.height(18.dp))
                Text(
                    "Izin belum diberikan. Aktifkan izin Musik di Pengaturan agar dapat memutar lagu.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFFFF5252)
                )
                TextButton(onClick = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }) {
                    Text("Buka Pengaturan", color = LiquidGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
