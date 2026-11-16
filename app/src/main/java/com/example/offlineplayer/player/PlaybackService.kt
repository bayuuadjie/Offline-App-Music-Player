package com.example.offlineplayer.player

import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.media3.session.DefaultMediaNotificationProvider
import com.example.offlineplayer.R

class PlaybackService : MediaSessionService() {

    companion object {
        const val ACTION_SLEEP_START = "SLEEP_START"
        const val ACTION_SLEEP_END_OF_SONG = "SLEEP_END_OF_SONG"
        const val ACTION_SLEEP_CANCEL = "SLEEP_CANCEL"
        const val KEY_MINUTES = "minutes"

        val CMD_SLEEP_START = SessionCommand(ACTION_SLEEP_START, Bundle.EMPTY)
        val CMD_SLEEP_END_OF_SONG = SessionCommand(ACTION_SLEEP_END_OF_SONG, Bundle.EMPTY)
        val CMD_SLEEP_CANCEL = SessionCommand(ACTION_SLEEP_CANCEL, Bundle.EMPTY)
    }

    private var session: MediaSession? = null
    private lateinit var exo: ExoPlayer
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private val callback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val commands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(CMD_SLEEP_START)
                .add(CMD_SLEEP_END_OF_SONG)
                .add(CMD_SLEEP_CANCEL)
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(commands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_SLEEP_START -> startTimer(args.getInt(KEY_MINUTES, 0))
                ACTION_SLEEP_END_OF_SONG -> {
                    cancelTimer()
                    exo.pauseAtEndOfMediaItems = true
                }
                ACTION_SLEEP_CANCEL -> cancelTimer()
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    override fun onCreate() {
        super.onCreate()
        val attrs = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exo = ExoPlayer.Builder(this)
            .setAudioAttributes(attrs, true)      // urus audio focus (telepon masuk dll.)
            .setHandleAudioBecomingNoisy(true)    // pause saat headset dicabut
            .setWakeMode(C.WAKE_MODE_LOCAL)       // tetap jalan saat layar mati
            .build()

        exo.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                // "Berhenti setelah lagu ini" sudah terpenuhi -> matikan mode itu
                if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
                    exo.pauseAtEndOfMediaItems = false
                }
            }
        })

        session = MediaSession.Builder(this, exo).setCallback(callback).build()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(this).apply { setSmallIcon(R.drawable.ic_snotif) }
        )
    }

    private fun startTimer(minutes: Int) {
        if (minutes <= 0) return
        cancelTimer()
        timerJob = scope.launch {
            delay(minutes * 60_000L)
            for (v in 10 downTo 0) {            // fade out 3 detik
                exo.volume = v / 10f
                delay(300)
            }
            exo.pause()
            exo.volume = 1f
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        exo.volume = 1f
        exo.pauseAtEndOfMediaItems = false
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = session?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}
