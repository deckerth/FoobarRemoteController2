@file:OptIn(ExperimentalMaterial3Api::class)

package com.deckerth.thomas.foobarremotecontroller2.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deckerth.thomas.foobarremotecontroller2.FoobarMediaService
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.page.CustomDevicePage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.DeviceSelectionPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.LayoutEditorMainPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.LayoutSelection
import com.deckerth.thomas.foobarremotecontroller2.ui.page.PlayingPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.PlaylistPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.SettingsPage
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.UpdatePreferences
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoScrollIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoscroll
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.getCurrentAlbumIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.initViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.playlistState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.updateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    //lateinit var imageLoader: ImageLoader

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = this
        initViewModel()
        //imageLoader = ImageLoader.Builder(mainActivity.baseContext).build() // Get your ImageLoader instance
        enableEdgeToEdge()
        setContent {
            // Read ip address and start observer
            UpdatePreferences()
            Foobar2000RemoteControllerTheme {
                if (this.isTablet()) {
                    FoobarTabletLayout()
                } else {
                    FoobarPhoneLayout()
                }
            }
        }
    }


    @Composable
    fun getCurrentRoute(navController: NavHostController) =
        navController.currentBackStackEntryAsState().value?.destination?.route

    fun navigateTo(route: String) {
        navController.navigate(route)
    }

    override fun onStart() {
        super.onStart()
        //start FoobarMediaSessionService
        val intent = Intent(this, FoobarMediaService::class.java)
        startForegroundService(intent)
    }

    @Composable
    fun FoobarPhoneLayout() {
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
                    title = { Text(text = _appBarLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    actions = {
                        when (getCurrentRoute(navController)) {
                            "Playlist" -> Row() {
                                FilledIconToggleButton(
                                    checked = autoscroll,
                                    onCheckedChange = {
                                        if (getCurrentAlbumIndex() != -1)
                                            autoscroll = !autoscroll
                                        if (autoscroll)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                playlistState.scrollToItem(
                                                    getCurrentAlbumIndex()
                                                )
                                                autoScrollIndex = getCurrentAlbumIndex()
                                            }
                                    })
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.jump_to_element),
                                        contentDescription = stringResource(R.string.desc_jump_to_title),
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    updateList()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh"
                                    )
                                }
                            }

                            "DeviceSelectionPage" -> Button(
                                onClick = {
                                    navigateTo("CustomDevicePage")
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.tune),
                                        contentDescription = stringResource(R.string.desc_play),
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height(26.dp)
                                    )
                                    Text(
                                        text = "Custom Device",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TopAppBarDefaults.topAppBarColors().scrolledContainerColor
                    )
                )
            },
            bottomBar = {
                if (getCurrentRoute(navController) in items.map { it.key }) {
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
                navController = navController,
                startDestination = "Now Playing",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("Playlist") {
                    _appBarLabel = appLabel
                    PlaylistPage()
                }
                composable("Now Playing") {
                    _appBarLabel = appLabel
                    PlayingPage()
                }
                composable("Settings") {
                    _appBarLabel = appLabel
                    SettingsPage()
                }
                composable("DeviceSelectionPage") {
                    _appBarLabel = stringResource(R.string.title_device_selection)
                    DeviceSelectionPage()
                }
                composable("CustomDevicePage") {
                    _appBarLabel = stringResource(R.string.title_device_selection)
                    CustomDevicePage()
                }
                composable("Layout selection") {
                    _appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutSelection()
                }
                composable("Layout editor") {
                    _appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutEditorMainPage()
                }

            }
        }
    }

    @Composable
    fun FoobarTabletLayout() {
        val navController = rememberNavController()
        _navController = navController
        appLabel = stringResource(R.string.app_name)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = _appBarLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    actions = {
                        when (getCurrentRoute(navController)) {
                            "Now Playing And Playlist" -> Row() {
                                FilledIconToggleButton(
                                    checked = autoscroll,
                                    onCheckedChange = {
                                        if (getCurrentAlbumIndex() != -1)
                                            autoscroll = !autoscroll
                                        if (autoscroll)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                playlistState.scrollToItem(
                                                    getCurrentAlbumIndex()
                                                )
                                                autoScrollIndex = getCurrentAlbumIndex()
                                            }
                                    })
                                {
                                    Icon(
                                        painter = painterResource(R.drawable.jump_to_element),
                                        contentDescription = stringResource(R.string.desc_jump_to_title),
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    updateList()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh"
                                    )
                                }
                                IconButton(onClick = {
                                    navigateTo("Settings")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }

                            "DeviceSelectionPage" -> Button(
                                onClick = {
                                    navigateTo("CustomDevicePage")
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.tune),
                                        contentDescription = stringResource(R.string.desc_play),
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .height(26.dp)
                                    )
                                    Text(
                                        text = "Custom Device",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TopAppBarDefaults.topAppBarColors().scrolledContainerColor
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "Now Playing And Playlist",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("Now Playing And Playlist") {
                    _appBarLabel = appLabel
                    Row(
                    ) { // Row for the two pages
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            PlaylistPage()
                        }
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            PlayingPage()
                        }
                    }
                }
                composable("Settings") {
                    _appBarLabel = appLabel
                    SettingsPage()
                }
                composable("DeviceSelectionPage") {
                    _appBarLabel = stringResource(R.string.title_device_selection)
                    DeviceSelectionPage()
                }
                composable("CustomDevicePage") {
                    _appBarLabel = stringResource(R.string.title_device_selection)
                    CustomDevicePage()
                }
                composable("Layout selection") {
                    _appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutSelection()
                }
                composable("Layout editor") {
                    _appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutEditorMainPage()
                }

            }
        }
    }

}

fun Context.isTablet(): Boolean {
    return (this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

