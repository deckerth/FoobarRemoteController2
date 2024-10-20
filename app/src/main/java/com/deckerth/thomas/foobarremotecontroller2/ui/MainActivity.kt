package com.deckerth.thomas.foobarremotecontroller2.ui

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deckerth.thomas.foobarremotecontroller2.FoobarMediaService
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.page.DeviceSelectionPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.PlayingPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.PlaylistPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.SettingsPage
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import me.zhanghai.compose.preference.defaultPreferenceFlow

data class BottomNavigationItem(
    val title: String,
    val key: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

lateinit var mainActivity: MainActivity

class MainActivity : ComponentActivity() {

    private var _navController: NavController? = null
    val navController get() = _navController!!

    private var _appBarLabel by mutableStateOf("Foobar Link")
    val appBarLabel get() = _appBarLabel

    private lateinit var appLabel: String

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = this
        enableEdgeToEdge()
        setContent {
            // Read ip address and start observer
            UpdatePreferences()
            Foobar2000RemoteControllerTheme {
                val navController = rememberNavController()
                _navController = navController
                appLabel = stringResource(R.string.app_name)
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
                        TopAppBar(
                            title = { Text(text = _appBarLabel ) },
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
                        if (navController.currentBackStackEntryAsState().value?.destination?.route in items.map { it.key }){
                            NavigationBar {
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        selected = selectedItemIndex == index,
                                        onClick = {
                                            selectedItemIndex = index
                                            navigateTo(item.key)
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
                            DeviceSelectionPage()
                        }

                    }
                }
            }
        }
    }

    fun navigateTo(route: String, label: String) {
        navController.navigate(route)
        _appBarLabel = label
    }

    fun navigateTo(route: String) {
        navController.navigate(route)
        _appBarLabel = appLabel
    }

    override fun onStart() {
        super.onStart()
        //start FoobarMediaSessionService
        val intent = Intent(this, FoobarMediaService::class.java)
        startForegroundService(intent)
    }
}