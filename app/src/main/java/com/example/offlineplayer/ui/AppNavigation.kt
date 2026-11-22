package com.example.offlineplayer.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlineplayer.ui.theme.DarkScreenGradient
import com.example.offlineplayer.ui.theme.LiquidGreen
import com.example.offlineplayer.ui.theme.LiquidGreenLight
import com.example.offlineplayer.viewmodel.PlayerViewModel

@Composable
private fun FloatingNav(
    tabs: List<Pair<String, ImageVector>>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val barShape = RoundedCornerShape(36.dp)

    // Floating Glass Dock Bar bergaya iPhone
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = barShape,
                ambientColor = Color.Black,
                spotColor = Color(0x401ED760)
            )
            .clip(barShape)
            .background(Color(0xD90E1712)) // Dark Frosted Glass
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.32f),
                        LiquidGreen.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                barShape
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { i, (label, icon) ->
                val sel = i == selected
                val itemShape = RoundedCornerShape(28.dp)

                val weight by animateFloatAsState(
                    targetValue = if (sel) 1.25f else 1f,
                    animationSpec = spring(
                        dampingRatio = 0.8f,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "tabWeight"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (sel) LiquidGreenLight else Color.White.copy(alpha = 0.52f),
                    animationSpec = tween(durationMillis = 220),
                    label = "tabContentColor"
                )

                val bgColor by animateColorAsState(
                    targetValue = if (sel) Color(0x2B1ED760) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220),
                    label = "tabBgColor"
                )

                val borderColor by animateColorAsState(
                    targetValue = if (sel) LiquidGreen.copy(alpha = 0.70f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220),
                    label = "tabBorderColor"
                )

                Box(
                    modifier = Modifier
                        .weight(weight)
                        .clip(itemShape)
                        .background(bgColor)
                        .border(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    borderColor,
                                    if (sel) Color.White.copy(alpha = 0.35f) else Color.Transparent
                                )
                            ),
                            itemShape
                        )
                        .clickable { onSelect(i) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(21.dp)
                        )
                        AnimatedVisibility(
                            visible = sel,
                            enter = fadeIn(animationSpec = tween(200, delayMillis = 30)) + expandHorizontally(
                                animationSpec = spring(
                                    dampingRatio = 0.8f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                expandFrom = Alignment.Start
                            ),
                            exit = fadeOut(animationSpec = tween(120)) + shrinkHorizontally(
                                animationSpec = spring(
                                    dampingRatio = 0.8f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                shrinkTowards = Alignment.Start
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(vm: PlayerViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var showNow by rememberSaveable { mutableStateOf(false) }
    var showQueue by rememberSaveable { mutableStateOf(false) }
    var showSleep by remember { mutableStateOf(false) }

    val tabs = listOf(
        "Lagu" to Icons.Filled.MusicNote,
        "Folder" to Icons.Filled.Folder,
        "Playlist" to Icons.Filled.LibraryMusic
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(DarkScreenGradient)
    ) {
        // Konten Layar Utama dengan animasi perpindahan tab halus
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.82f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        initialOffsetX = { fullWidth -> direction * (fullWidth / 3) }
                    ) + fadeIn(
                        animationSpec = tween(280, easing = LinearOutSlowInEasing)
                    )).togetherWith(
                        slideOutHorizontally(
                            animationSpec = spring(
                                dampingRatio = 0.85f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            targetOffsetX = { fullWidth -> -direction * (fullWidth / 3) }
                        ) + fadeOut(
                            animationSpec = tween(200, easing = FastOutLinearInEasing)
                        )
                    )
                },
                label = "tabScreenContent"
            ) { currentTab ->
                when (currentTab) {
                    0 -> SongsScreen(vm)
                    1 -> FoldersScreen(vm)
                    else -> PlaylistsScreen(vm)
                }
            }
        }

        // Floating Bottom Controls ala iPhone (MiniPlayer + Floating Glass Dock + Home Bar)
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
        ) {
            AnimatedVisibility(
                visible = vm.currentSong != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(animationSpec = tween(240)) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeOut(animationSpec = tween(180)) + shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            ) {
                MiniPlayer(vm) { showNow = true }
            }
            FloatingNav(tabs, tab) { tab = it }
            // Indikator Home Bar ala iPhone
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(110.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
            )
        }

        // Layar Now Playing dengan animasi slide-up (besar/kecilkan bar halus tanpa ngejedak)
        AnimatedVisibility(
            visible = showNow,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(220)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(animationSpec = tween(180))
        ) {
            NowPlayingScreen(
                vm = vm,
                onClose = { showNow = false },
                onOpenQueue = { showQueue = true },
                onOpenSleep = { showSleep = true }
            )
        }

        // Layar Antrian dengan animasi slide-up halus
        AnimatedVisibility(
            visible = showQueue,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(220)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(animationSpec = tween(180))
        ) {
            QueueScreen(vm) { showQueue = false }
        }
    }

    if (showSleep) {
        SleepTimerDialog(vm) { showSleep = false }
    }
}
