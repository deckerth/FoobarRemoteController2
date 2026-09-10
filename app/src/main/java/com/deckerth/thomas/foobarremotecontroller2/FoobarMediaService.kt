package com.deckerth.thomas.foobarremotecontroller2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.VolumeProviderCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.appViewModel
import kotlinx.coroutines.runBlocking
import java.time.Instant

var mediaSession: MediaSessionCompat? = null
var foobarMediaService: FoobarMediaService? = null
lateinit var volumeProvider: VolumeProviderCompat
var lastChanged: Instant = Instant.now()


fun enableVolumeControl() {
    if (mediaSession != null) {
        mediaSession!!.setPlaybackToRemote(volumeProvider)
    }
}

class FoobarMediaService : Service() {
    private lateinit var audioManager: AudioManager
    private lateinit var viewModelInstance: AppViewModel

    private val audioFocusRequest: AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener { focusChange ->
                var pause: Boolean
                runBlocking {
                    pause = getPauseDuringPhoneCallsBlocking()
                }

                if (pause)
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            viewModelInstance.playerAccess.pausePlayback()
                            // Handle audio focus loss (e.g., stop playback)
                        }

                        AudioManager.AUDIOFOCUS_GAIN -> {
                            viewModelInstance.playerAccess.startPlayback()
                            // Handle audio focus gain (e.g., resume playback)
                        }
                    }
            }

            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(USAGE_MEDIA)
                    .setContentType(CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()

    companion object {
        const val CHANNEL_ID = "MediaServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()

        foobarMediaService = this

        if (appViewModel == null) {
            viewModelInstance = AppViewModel("FoobarMediaService")
            viewModelInstance.initialize()
        } else
            viewModelInstance = appViewModel!!

        // Initialize AudioManager and MediaSessionCompat
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Initialize the MediaSession
        mediaSession = MediaSessionCompat(this, "FoobarMediaService").apply {

            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_NONE, 0, 1f)
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                    .build()
            )

            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    // Handle play action
                    viewModelInstance.playerAccess.startPlayback()
                    //updateNotification(true)
                }

                override fun onPause() {
                    // Handle pause action
                    viewModelInstance.playerAccess.pausePlayback()
                    //updateNotification(false)
                }

                override fun onSkipToNext() {
                    // Handle skip to next action
                    viewModelInstance.playerAccess.nextTrack()
                }

                override fun onSkipToPrevious() {
                    // Handle skip to previous action
                    viewModelInstance.playerAccess.previousTrack()
                }
            })

            isActive = true

            volumeProvider = object : VolumeProviderCompat(
                VOLUME_CONTROL_ABSOLUTE,
                100, // Max volume
                viewModelInstance.foobVolumeControl.currentValuePercent
            ) {
                override fun onSetVolumeTo(volume: Int) {
                    lastChanged = Instant.now()
                    currentVolume = volume
                    viewModelInstance.foobVolumeControl.value =
                        viewModelInstance.foobVolumeControl.getDecibelValue(currentVolume)
                    viewModelInstance.playerAccess.setVolume(viewModelInstance.foobVolumeControl.value)
                }

                override fun onAdjustVolume(direction: Int) {
                    lastChanged = Instant.now()
                    currentVolume += direction
                    viewModelInstance.foobVolumeControl.value =
                        viewModelInstance.foobVolumeControl.getDecibelValue(currentVolume)
                    viewModelInstance.playerAccess.setVolume(viewModelInstance.foobVolumeControl.value)
                }
            }

            var volumeControlEnabled: Boolean
            runBlocking { volumeControlEnabled = getFoobarVolumeControlBlocking() }

            if (volumeControlEnabled)
                setPlaybackToRemote(volumeProvider)
        }
    }

    fun updateNotification() {
        startForeground(NOTIFICATION_ID, createNotification())
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service as a foreground service
        updateNotification()
        requestAudioFocus()

        return START_NOT_STICKY // prevent that Android restarts the service if it is killed
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Media Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(MediaStyle().setMediaSession(mediaSession!!.sessionToken))
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession!!.release()
        mediaSession = null
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestAudioFocus(): Boolean {

        return audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}