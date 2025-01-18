@file:OptIn(ExperimentalMaterial3Api::class)

package com.deckerth.thomas.foobarremotecontroller2.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.deckerth.thomas.foobarremotecontroller2.getIpAddressBlocking
import com.deckerth.thomas.foobarremotecontroller2.model.PlaybackState
import com.deckerth.thomas.foobarremotecontroller2.model.checkIpAddressSyntax
import com.deckerth.thomas.foobarremotecontroller2.ui.components.TitleDetails
import com.deckerth.thomas.foobarremotecontroller2.ui.page.BrowserMainPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.LayoutEditorMainPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.LayoutSelection
import com.deckerth.thomas.foobarremotecontroller2.ui.page.PlayingPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.PlaylistPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.SettingsPage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.WelcomePage
import com.deckerth.thomas.foobarremotecontroller2.ui.page.WizardPage
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.UpdatePreferences
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoScrollIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.autoscroll
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.filterValue
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.getCurrentAlbumIndex
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.initViewModel
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.player
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.playlistState
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.showFilter
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
    private val navController get() = _navController!!

    var appBarLabel by mutableStateOf("Foobar Link")

    private lateinit var appLabel: String

    //lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = this
        initViewModel()
        enableEdgeToEdge()
        setContent {
            // Read ip address and start observer
            var ipAddress by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                ipAddress = getIpAddressBlocking()
            }

            UpdatePreferences()
            BackPressHandler()
            if (ipAddress.isEmpty())
                BlackPage()
            else
                Foobar2000RemoteControllerTheme {
                    if (!checkIpAddressSyntax(ipAddress)) {
                        WelcomePage()
                    } else if (this.isTablet()) {
                        FoobarTabletLayout()
                    } else {
                        FoobarPhoneLayout()
                    }
                }
        }
    }

    fun restartService() {
        val intent = Intent(this, FoobarMediaService::class.java)
        mainActivity.stopService(intent)
        startForegroundService(intent)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BlackPage() {
        Foobar2000RemoteControllerTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = mainActivity.appBarLabel) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) { }
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FoobarPhoneLayout() {
        val navController = rememberNavController()
        _navController = navController
        appLabel = stringResource(R.string.app_name)
        var dropdownMenuExpanded by remember { mutableStateOf(false) }
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
            mutableIntStateOf(1)
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = appBarLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        if (getCurrentRoute(navController) == "Browser")
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = stringResource(R.string.button_back)
                                )
                            }
                    },
                    actions = {
                        when (getCurrentRoute(navController)) {
                            "Playlist" -> Row {
                                FilledIconToggleButton(
                                    checked = autoscroll,
                                    onCheckedChange = {
                                        if (getCurrentAlbumIndex() != -1)
                                            autoscroll = !autoscroll
                                        if (autoscroll && getCurrentAlbumIndex() != -1)
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
                                if (filterValue.value.isNotEmpty())
                                    IconButton(onClick = {
                                        showFilter.value = false
                                        filterValue.value = ""
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.filter_alt_off),
                                            contentDescription = "Filter off"
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
                                IconToggleButton(
                                    checked = dropdownMenuExpanded,
                                    onCheckedChange = {
                                        dropdownMenuExpanded = !dropdownMenuExpanded
                                    },
                                    colors = IconToggleButtonColors(
                                        checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        checkedContentColor = IconButtonDefaults.iconButtonColors().contentColor,
                                        containerColor = IconButtonDefaults.iconButtonColors().containerColor,
                                        contentColor = IconButtonDefaults.iconButtonColors().contentColor,
                                        disabledContainerColor = IconButtonDefaults.iconButtonColors().disabledContainerColor,
                                        disabledContentColor = IconButtonDefaults.iconButtonColors().disabledContentColor,
                                    )
                                )
                                {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More"
                                    )

                                }
                                DropdownMenu(
                                    expanded = dropdownMenuExpanded,
                                    onDismissRequest = { dropdownMenuExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.menu_item_add_titles)) },
                                        onClick = {
                                            dropdownMenuExpanded = false; navigateTo("Browser")
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add music"
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.filter_titles)) },
                                        onClick = {
                                            dropdownMenuExpanded = false; showFilter.value = true
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(R.drawable.filter_alt),
                                                contentDescription = "Filter"
                                            )
                                        }
                                    )
                                }
                            }

                            "Now Playing" -> Row {
                                val infoButtonClicked = remember { mutableStateOf(false) }
                                IconButton(
                                    onClick = { infoButtonClicked.value = true },
                                    enabled = player != null && player!!.playbackState != PlaybackState.STOPPED
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.info_i),
                                        contentDescription = "Refresh"
                                    )
                                }
                                if (player != null && infoButtonClicked.value)
                                    TitleDetails(
                                        player = player!!,
                                        onDismiss = { infoButtonClicked.value = false })
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
                                    navController.popBackStack()
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
                    appBarLabel = appLabel
                    PlaylistPage()
                }
                composable("Now Playing") {
                    appBarLabel = appLabel
                    PlayingPage()
                }
                composable("Settings") {
                    appBarLabel = appLabel
                    SettingsPage()
                }
                composable("DeviceSelectionPage") {
                    appBarLabel = stringResource(R.string.title_device_selection)
                    WizardPage(
                        showIntroduction = false,
                        onCancel = {
                            navController.navigateUp()
                        },
                        onFinished = {
                            navController.navigateUp()
                        })
                }
                composable("Layout selection") {
                    appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutSelection()
                }
                composable("Layout editor") {
                    appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutEditorMainPage()
                }
                composable("Browser") {
                    appBarLabel = stringResource(R.string.browser)
                    BrowserMainPage()
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
                    title = {
                        Row {
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = appBarLabel
                            )
                            if (getCurrentRoute(navController) == "Now Playing And Playlist") {
                                FilledIconToggleButton(
                                    modifier = Modifier.padding(start = 10.dp),
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
                                    navigateTo("Browser")
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add music"
                                    )
                                }
                                if (filterValue.value.isNotEmpty())
                                    IconButton(onClick = {
                                        showFilter.value = false
                                        filterValue.value = ""
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.filter_alt_off),
                                            contentDescription = "Filter off"
                                        )
                                    }
                                else
                                    IconButton(onClick = {
                                        showFilter.value = true
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.filter_alt),
                                            contentDescription = "Filter"
                                        )
                                    }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = {
                        if (getCurrentRoute(navController) == "Browser" || getCurrentRoute(
                                navController
                            ) == "Settings"
                        )
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = stringResource(R.string.button_back)
                                )
                            }
                    },
                    actions = {
                        var infoButtonClicked by remember { mutableStateOf(false) }
                        when (getCurrentRoute(navController)) {
                            "Now Playing And Playlist" -> Row {
                                IconButton(
                                    onClick = { infoButtonClicked = true },
                                    enabled = player != null && player!!.playbackState != PlaybackState.STOPPED
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.info_i),
                                        contentDescription = stringResource(R.string.button_details),
                                    )
                                }
                                if (infoButtonClicked)
                                    TitleDetails(
                                        player = player!!,
                                        onDismiss = { infoButtonClicked = false },
                                    )
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
                    appBarLabel = appLabel
                    Row { // Row for the two pages
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
                    appBarLabel = stringResource(R.string.title_settings)
                    SettingsPage()
                }
                composable("DeviceSelectionPage") {
                    appBarLabel = stringResource(R.string.title_device_selection)
                    WizardPage(
                        showIntroduction = false,
                        onCancel = {
                            navController.navigateUp()
                        },
                        onFinished = {
                            navController.navigateUp()
                        })
                }
                composable("Layout selection") {
                    appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutSelection()
                }
                composable("Layout editor") {
                    appBarLabel = stringResource(R.string.choose_layout_to_change)
                    LayoutEditorMainPage()
                }
                composable("Browser") {
                    appBarLabel = stringResource(R.string.browser)
                    BrowserMainPage()
                }
            }
        }
    }

    @Composable
    fun BackPressHandler() {
        var backPressedTime by remember { mutableLongStateOf(0L) }
        val context = LocalContext.current

        BackHandler {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                Process.killProcess(Process.myPid())
            } else {
                backPressedTime = System.currentTimeMillis()
                Toast.makeText(
                    context,
                    getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

fun Context.isTablet(): Boolean {
    return (this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

