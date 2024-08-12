package com.deckerth.thomas.foobarremotecontroller2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.windowInsets
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import me.zhanghai.compose.preference.defaultPreferenceFlow

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            updatePreferences(defaultPreferenceFlow())
            Foobar2000RemoteControllerTheme {
                val navController = rememberNavController()
                val items = listOf(
                    BottomNavigationItem(
                        title = "Playlist",
                        selectedIcon = Icons.AutoMirrored.Filled.List,
                        unselectedIcon = Icons.AutoMirrored.Outlined.List,
                    ),
                    BottomNavigationItem(
                        title = "Now Playing",
                        selectedIcon = Icons.Filled.PlayArrow,
                        unselectedIcon = Icons.Outlined.PlayArrow,
                    ),
                    BottomNavigationItem(
                        title = "Settings",
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
                                        navController.navigate(item.title)
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
                        navController = navController,
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

                    }
                }
            }
        }
    }
}