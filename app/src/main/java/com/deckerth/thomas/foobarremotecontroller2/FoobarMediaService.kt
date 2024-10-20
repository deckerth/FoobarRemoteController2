package com.deckerth.thomas.foobarremotecontroller2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.VolumeProviderCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.deckerth.thomas.foobarremotecontroller2.connector.PlayerAccess
import com.deckerth.thomas.foobarremotecontroller2.ui.foobVolumeControl

var mediaSession: MediaSessionCompat? = null
var foobarMediaService: FoobarMediaService? = null

class FoobarMediaService : Service() {

    private lateinit var audioManager: AudioManager

    companion object {
        const val CHANNEL_ID = "MediaServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        foobarMediaService = this

        // Initialize AudioManager and MediaSessionCompat
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
                    PlayerAccess.getInstance().startPlayback()
                    //updateNotification(true)
                }

                override fun onPause() {
                    // Handle pause action
                    PlayerAccess.getInstance().pausePlayback()
                    //updateNotification(false)
                }

                override fun onSkipToNext() {
                    // Handle skip to next action
                    PlayerAccess.getInstance().nextTrack()
                }

                override fun onSkipToPrevious() {
                    // Handle skip to previous action
                    PlayerAccess.getInstance().previousTrack()
                }

                override fun onStop() {
                    // Handle stop action
                    setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
                            .setActions(
                                PlaybackStateCompat.ACTION_PLAY or
                                        PlaybackStateCompat.ACTION_PLAY_PAUSE

                            )
                            .build()
                    )
                    //updateNotification(false)
                }
            })

            isActive = true

            val volumeProvider = object : VolumeProviderCompat(
                VOLUME_CONTROL_ABSOLUTE,
                100, // Max volume
                foobVolumeControl.currentValuePercent
            ) {
                override fun onSetVolumeTo(volume: Int) {
                    currentVolume = volume
                    foobVolumeControl.value = foobVolumeControl.getDecibelValue(currentVolume)
                    PlayerAccess.getInstance().setVolume(foobVolumeControl.value)
                }

                override fun onAdjustVolume(direction: Int) {
                    currentVolume += direction
                    foobVolumeControl.value = foobVolumeControl.getDecibelValue(currentVolume)
                    PlayerAccess.getInstance().setVolume(foobVolumeControl.value)
                }
            }

            setPlaybackToRemote(volumeProvider)
        }
    }

    fun updateNotification(){
        startForeground(NOTIFICATION_ID, createNotification())
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start the service as a foreground service
        updateNotification()
        requestAudioFocus()

        return START_STICKY
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
        audioManager.abandonAudioFocus(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        PlayerAccess.getInstance().pausePlayback()
                        // Handle audio focus loss (e.g., stop playback)
                    }

                    AudioManager.AUDIOFOCUS_GAIN -> {
                        PlayerAccess.getInstance().startPlayback()
                        // Handle audio focus gain (e.g., resume playback)
                    }
                }
            },
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}