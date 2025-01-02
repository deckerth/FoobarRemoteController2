@file:OptIn(ExperimentalMaterial3Api::class)
package com.deckerth.thomas.foobarremotecontroller2.ui.page

import com.deckerth.thomas.foobarremotecontroller2.ui.components.WelcomeText

import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.saveIpAddress
import com.deckerth.thomas.foobarremotecontroller2.ui.MainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme



internal class WelcomePageViewModel {
    var navController: NavHostController? = null
    var appBarLabel: String by mutableStateOf("Welcome")
}

private val viewModel = WelcomePageViewModel()

@Composable
fun WelcomePage() {
    viewModel.navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = viewModel.appBarLabel) },
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TopAppBarDefaults.topAppBarColors().scrolledContainerColor
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = viewModel.navController!!,
            startDestination = "Introduction",
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() },
        ) {
            composable("Introduction") {
                viewModel.appBarLabel = stringResource(R.string.welcome_heading)
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WelcomeText(Modifier.padding(16.dp))
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = {}
                        ) {
                            Text(stringResource(id = R.string.button_check_connection))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                viewModel.navController?.navigate("Search Device")
                            },
                        ) {
                            Text(stringResource(id = R.string.button_save))
                        }
                    }
                }

            }
            composable("Search Device") {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DeviceSelectionPage(
                        onClick = { device: Device ->
                            saveIpAddress("${device.ipAddress}:8880", mainActivity)
                        }
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = {}
                        ) {
                            Text(stringResource(id = R.string.button_check_connection))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                viewModel.navController?.navigate("Select Device")
                            },
                        ) {
                            Text(stringResource(id = R.string.button_save))
                        }
                    }
                }
            }
        }
    }

}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun WelcomePagePreview() {
    Foobar2000RemoteControllerTheme {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                WelcomePage()
            }
        }
    }
}
