package com.deckerth.thomas.foobarremotecontroller2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import me.zhanghai.compose.preference.defaultPreferenceFlow

data class BottomNavigationItem(
    val title: String,
    val key: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

lateinit var navController: NavController

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Read ip address and start observer
            updatePreferences(defaultPreferenceFlow())
            Foobar2000RemoteControllerTheme {
                navController = rememberNavController()
                val items = listOf(
                    BottomNavigationItem(
                        title = stringResource(R.string.tab_playlist),
                        key = "Playlist",
                        selectedIcon = Icons.AutoMirrored.Filled.List,
                        unselectedIcon = Icons.AutoMirrored.Outlined.List,
                    ),
                    BottomNavigationItem(
                        title = stringResource(R.string.tab_now_playing),
                        key = "Now Playing",
                        selectedIcon = Icons.Filled.PlayArrow,
                        unselectedIcon = Icons.Outlined.PlayArrow,
                    ),
                    BottomNavigationItem(
                        title = stringResource(R.string.tab_settings),
                        key = "Settings",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                    ),
                )
                var selectedItemIndex by rememberSaveable {
                    mutableStateOf(1)
                }
                Scaffold(
                    topBar = {
                        val context = LocalContext.current
                        val appLabel = context.applicationInfo.loadLabel(context.packageManager).toString()
                        TopAppBar(
                            title = { Text(text = appLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            actions = {
                                if (selectedItemIndex == 0)
                                    IconButton(onClick = {
                                        updateList()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh"
                                        )
                                    }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = TopAppBarDefaults.topAppBarColors().scrolledContainerColor
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
                                    selected = selectedItemIndex == index,
                                    onClick = {
                                        selectedItemIndex = index
                                        navController.navigate(item.key)
                                    },
                                    label = {
                                        Text(text = item.title)
                                    },
                                    alwaysShowLabel = true,
                                    icon = {
                                        Icon(
                                            imageVector = if (index == selectedItemIndex) {
                                                item.selectedIcon
                                            } else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController as NavHostController,
                        startDestination = "Now Playing",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("Playlist") {
                            PlaylistPage()
                        }
                        composable("Now Playing") {
                            PlayingPage()
                        }
                        composable("Settings") {
                            SettingsPage()
                        }
                        composable("DeviceSelectionPage") {
                            DeviceSelectionPage(this@MainActivity)
                        }

                    }
                }
            }
        }
//        val mediaSession = MediaSessionCompat(this , "FoobarActivity").apply {
//            // Set the initial playback state
//            setPlaybackState(
//                PlaybackStateCompat.Builder()
//                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f)
//                    .setActions(
//                        PlaybackStateCompat.ACTION_PLAY or
//                                PlaybackStateCompat.ACTION_PLAY_PAUSE
//                    )
//                    .build()
//            )
//
//            val volumeProvider = object : VolumeProviderCompat(
//                VolumeProviderCompat.VOLUME_CONTROL_ABSOLUTE,
//                100, // Max volume level
//                50   // Initial volume level
//            ) {
//                override fun onSetVolumeTo(volume: Int) {
//
//                }
//
//                override fun onAdjustVolume(direction: Int) {
//                }
//            }
//
//            setPlaybackToRemote(volumeProvider)
//        }
//        mediaSession.isActive = true
////        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
////        audioManager.requestAudioFocus(
////            AudioManager.OnAudioFocusChangeListener { focusChange ->
////
////            },
////            AudioManager.STREAM_MUSIC,
////            AudioManager.AUDIOFOCUS_GAIN
////        )
    }

    override fun onStart() {
        super.onStart()
        //start FoobarMediaSessionService
        val intent = Intent(this, FoobarMediaService::class.java)
        startForegroundService(intent)
    }
}