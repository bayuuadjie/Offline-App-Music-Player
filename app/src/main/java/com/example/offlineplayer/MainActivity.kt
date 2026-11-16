package com.example.offlineplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.offlineplayer.ui.AppNavigation
import com.example.offlineplayer.ui.PermissionScreen
import com.example.offlineplayer.ui.theme.OfflinePlayerTheme

class MainActivity : ComponentActivity() {

    private var granted by mutableStateOf(false)

    private fun hasAudioPermission(): Boolean {
        val perm = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        granted = hasAudioPermission()
        setContent {
            OfflinePlayerTheme {
                if (granted) AppNavigation() else PermissionScreen { granted = true }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        granted = hasAudioPermission()   // jaga-jaga kalau izin diberikan lewat Pengaturan
    }
}
