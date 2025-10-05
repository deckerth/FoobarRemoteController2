package com.deckerth.thomas.foobarremotecontroller2.ui.page

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.model.checkIpAddressSyntax
import com.deckerth.thomas.foobarremotecontroller2.model.checkIpSyntax
import com.deckerth.thomas.foobarremotecontroller2.model.checkPortSyntax
import com.deckerth.thomas.foobarremotecontroller2.model.isWlanConnected
import com.deckerth.thomas.foobarremotecontroller2.saveIpAddress
import com.deckerth.thomas.foobarremotecontroller2.ui.components.DeviceNotFoundText
import com.deckerth.thomas.foobarremotecontroller2.ui.components.WelcomeText
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URL

data class Device(
    val hostName: String? = null,
    val ipAddress: String
) {

    val isValid: Boolean
        get() {
            return checkIpAddressSyntax(ipAddress)
        }
}

internal class WizardState {
    var navController: NavHostController? = null
}

private val state = WizardState()

private var loading by mutableStateOf(true)
val devices = mutableStateListOf<Device>()

@Composable
fun WizardPage(
    vm: AppViewModel? = null,
    modifier: Modifier = Modifier,
    showIntroduction: Boolean = true,
    onCancel: () -> Unit = {},
    onFinished: () -> Unit = {}
) {
    state.navController = rememberNavController()
    WizardPageBackPressHandler()
    NavHost(
        navController = state.navController!!,
        startDestination = if (showIntroduction) "Introduction" else "Search Device",
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() },
    ) {
        composable("Introduction") {
            if (state.navController != null)
                println("FOOB navstack: ${state.navController!!.graph.nodes.size()}")
            mainActivity.appBarLabel = stringResource(R.string.welcome_heading)
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
                        onClick = onCancel
                    ) {
                        Text(stringResource(id = android.R.string.cancel))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            state.navController?.navigate("Search Device")
                        },
                    ) {
                        Text(stringResource(id = R.string.button_next))
                    }
                }
            }

        }
        composable("Search Device") {
            if (state.navController != null)
                println("FOOB navstack: ${state.navController!!.graph.nodes.size()}")
            mainActivity.appBarLabel = stringResource(R.string.search_device_heading)
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                var hasSearched by remember { mutableStateOf(false) }
                SearchDevices(
                    onFinishedLoading = {
                        hasSearched = true
                    },
                    onFinished = { device: Device ->
                        saveIpAddress("${device.ipAddress}:8880", mainActivity)
                        vm?.ipAddress = "${device.ipAddress}:8880"
                        vm?.credentialsManager?.setIPAddress(vm.ipAddress!!)
                        vm?.playlistsViewModel?.restartObserver()
                        onFinished()
                    }
                )
                if (hasSearched) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        FilledTonalButton(
                            onClick = {
                                prepareDeviceSelectionPage()
                                hasSearched = false
                                state.navController!!.navigate("Search Device")
                            }
                        ) {
                            Text(stringResource(id = R.string.button_refresh_roots))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                state.navController?.navigate("Manual Device")
                            },
                        ) {
                            Text(stringResource(id = R.string.button_next))
                        }
                    }
                }
            }
        }
        composable("No Device Found") {
            if (state.navController != null)
                println("FOOB navstack: ${state.navController!!.graph.nodes.size()}")
            mainActivity.appBarLabel = stringResource(R.string.device_not_found_heading)
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                DeviceNotFoundText(Modifier.padding(16.dp))
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = {
                            prepareDeviceSelectionPage()
                            state.navController!!.navigateUp()
                        }
                    ) {
                        Text(stringResource(id = R.string.button_refresh_roots))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            state.navController?.navigate("Manual Device")
                        },
                    ) {
                        Text(stringResource(id = R.string.button_next))
                    }
                }
            }
        }
        composable("Manual Device") {
            if (state.navController != null)
                println("FOOB navstack: ${state.navController!!.graph.nodes.size()}")
            mainActivity.appBarLabel = stringResource(R.string.manual_device_heading)
            CustomDevicePage(onFinished = { device: Device ->
                if (device.isValid) {
                    saveIpAddress(device.ipAddress, mainActivity)
                    vm?.ipAddress = device.ipAddress
                    vm?.credentialsManager?.setIPAddress(device.ipAddress)
                    vm?.playlistsViewModel?.restartObserver()
                    onFinished()
                }
            })
        }
    }
}

@Composable
fun WizardPageBackPressHandler() {
    BackHandler {
        state.navController?.popBackStack()
    }
}

@Composable
fun SearchDevices(
    modifier: Modifier = Modifier,
    onFinishedLoading: () -> Unit = {},
    onFinished: (device: Device) -> Unit
) {
    // State to hold whether the search has already been done
    var hasSearched by rememberSaveable { mutableStateOf(false) }

    // LaunchedEffect triggers when the key changes (here, `true`)
    LaunchedEffect(Unit) {
        if (!hasSearched) {
            loading = true
            prepareDeviceSelectionPage()
        }
    }
    LaunchedEffect(loading) {
        if (!loading) {
            onFinishedLoading()
            hasSearched = true
            if (devices.isEmpty())
                state.navController!!.navigate("No Device Found")
        }
    }
    Column(modifier) {
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        DeviceList(devices = devices) {
            onFinished(it)
        }
    }
}

fun prepareDeviceSelectionPage() {
    devices.clear()
    CoroutineScope(Dispatchers.IO).launch {
        runBlocking {
            scanForFoobarServers(8880)
        }
    }
}

suspend fun scanForFoobarServers(port: Int, timeout: Int = 1000) {
    val baseIp: String
    try {
        baseIp = getLocalIpBase()
    } catch (_: Exception) {
        return
    }

    loading = true
    coroutineScope {
        val jobs = (1..254).map { i ->
            launch(Dispatchers.IO) {
                val ip = "$baseIp.$i"
                println("FOOB $ip")
                try {
                    if (isFoobarServer(ip, port, timeout)) {
                        val hostName = getHostName(ip)
                        val element = Device(hostName, ip)
                        println("FOOB Success $element")
                        devices.add(element)
                    }
                } catch (_: Exception) {
                }
            }
        }
        jobs.joinAll()
        loading = false
    }
}

private fun getLocalIpBase(): String {
    val networkInterfaces = NetworkInterface.getNetworkInterfaces()
    networkInterfaces.iterator().forEach { networkInterface ->
        networkInterface.inetAddresses.iterator().forEach { inetAddress ->
            if (!inetAddress.isLoopbackAddress && inetAddress.hostAddress?.contains('.') == true) {
                val ip = inetAddress.hostAddress
                if (ip != null) {
                    return ip.substringBeforeLast(".")
                }
            }
        }
    }
    throw IllegalStateException("Cannot determine the local IP base.")
}

private fun isFoobarServer(ip: String, port: Int, timeout: Int): Boolean {
    try {
        val url = URL("http://$ip:$port/api/player")
        println(url)
        with(url.openConnection() as HttpURLConnection) {
            connectTimeout = timeout
            readTimeout = timeout
            requestMethod = "GET"

            connect()
            return responseCode == 200 || responseCode == 401
        }
    } catch (e: Exception) {
        return false
    }
}

private fun getHostName(ip: String): String? {
    return try {
        InetAddress.getByName(ip).hostName
    } catch (e: Exception) {
        null
    }
}


@Composable
fun DeviceList(devices: List<Device>, onClick: (device: Device) -> Unit = {}) {
    if (isWlanConnected(mainActivity))
        LazyColumn {
            items(devices) { device ->
                DeviceEntry(device, onClick)
            }
        }
    else
        Text(
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(id = R.string.no_wifi_connection)
        )
}

@Composable
fun DeviceEntry(device: Device, onClick: (device: Device) -> Unit = {}) {
    Column(
        modifier = Modifier
            .clickable {
                onClick(device)
            }
            .padding(16.dp)
            .height(40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (device.hostName == null) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                text = device.ipAddress,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                text = device.hostName,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = device.ipAddress,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun DevicePreview() {
    val device = Device(
        "obsidian.fritz.box",
        "192.168.178.103:8880"
    )
    DeviceEntry(device = device)
}

@Preview(
    showBackground = true
)
@Composable
fun DeviceListPreview() {
    val device = Device(
        hostName = "Obsidian.fritz.box",
        ipAddress = "192.168.178.103:8880"
    )
    val devices = listOf(
        device,
        device,
        device,
        device
    )
    DeviceList(devices)

}

@Composable
fun CustomDevicePage(
    onFinished: (device: Device) -> Unit
) {
    var ip by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("8880") }
    var device by remember { mutableStateOf(Device(ipAddress = "$ip:$port")) }
    device = Device(ipAddress = "$ip:$port")

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                isError = !checkIpSyntax(ip),
                value = ip,
                maxLines = 1,
                onValueChange = { value ->
                    ip = value.replace(Regex("[\r\n]+"), "")
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.field_ip_address)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                isError = !checkPortSyntax(port),
                value = port,
                maxLines = 1,
                onValueChange = { value ->
                    port = value.replace(Regex("[\r\n]+"), "")
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.field_port)) }
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                enabled = device.isValid,
                onClick = {
                    Toast.makeText(mainActivity, mainActivity.getString(R.string.checking_connection), Toast.LENGTH_SHORT).show()
                    // Launch a coroutine on the IO dispatcher for network operations
                    CoroutineScope(Dispatchers.IO).launch {
                        val success: Boolean = try {
                            isFoobarServer(ip, port.toInt(), 1000)
                        } catch (e: Exception) {
                            false
                        }

                        // Switch back to the main thread to show the Toast messages
                        withContext(Dispatchers.Main) {
                            if (success) {
                                Toast.makeText(
                                    mainActivity,
                                    mainActivity.getString(R.string.connection_successful),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    mainActivity,
                                    mainActivity.getString(R.string.connection_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
            ) {
                Text(stringResource(id = R.string.button_check_connection))
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                enabled = device.isValid,
                onClick = {
                    onFinished(device)
                }
            ) {
                Text(stringResource(id = R.string.button_save))
            }
        }
    }

}

@Preview(
    showBackground = true
)
@Composable
private fun CustomDevicePagePreview() {
    CustomDevicePage(onFinished = {})
}

@Preview
@Composable
fun DeviceSelectionPagePreview() {
    WizardPage()
}