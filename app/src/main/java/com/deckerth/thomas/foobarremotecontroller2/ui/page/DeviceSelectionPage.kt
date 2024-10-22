package com.deckerth.thomas.foobarremotecontroller2.ui.page

import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.saveIpAddress
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
)

private var loading by mutableStateOf(true)
val devices = mutableStateListOf<Device>()

@Composable
fun DeviceSelectionPage(){
    // State to hold whether the search has already been done
    var hasSearched by rememberSaveable { mutableStateOf(false) }

    // LaunchedEffect triggers when the key changes (here, `true`)
    LaunchedEffect(Unit) {
        if (!hasSearched) {
            hasSearched = true
            prepareDeviceSelectionPage()
        }
    }
    Column{
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        DeviceList(devices = devices)
    }

}

fun prepareDeviceSelectionPage(){
    devices.clear()
    Thread{
        runBlocking {
            scanForFoobarServers(8880)
        }
    }.start()
}


suspend fun scanForFoobarServers(port: Int, timeout: Int = 1000) {
    loading = true
    val baseIp = getLocalIpBase()

    coroutineScope {
        val jobs = (1..254).map { i ->
            launch(Dispatchers.IO) {
                val ip = "$baseIp.$i"
                println("FOOB $ip")
                if (isFoobarServer(ip, port, timeout)) {
                    val hostName = getHostName(ip)
                    val element = Device(hostName, ip)
                    println("FOOB Success $element")
                    devices.add(element)
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
            if (!inetAddress.isLoopbackAddress && inetAddress.hostAddress.contains('.')) {
                val ip = inetAddress.hostAddress
                return ip.substringBeforeLast(".")
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
            return responseCode == 200
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
fun DeviceList(devices: List<Device>){
    LazyColumn {
        items(devices) { device ->
            DeviceEntry(device)
        }
    }
}

@Composable
fun DeviceEntry(device: Device){
        Column(
            modifier = Modifier
                .clickable {
                    saveIpAddress("${device.ipAddress}:8880", mainActivity)
                    mainActivity.navigateTo("Settings")
                }
                .padding(16.dp)
                .height(40.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (device.hostName == null){
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    text = device.ipAddress,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge
                )
            }else {
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
fun DevicePreview(){
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
fun DeviceListPreview(){
    val device = Device(
        hostName = "Obsidian.fritz.box",
        ipAddress = "192.168.178.103:8880"
    )
    val devices = listOf<Device>(
        device,
        device,
        device,
        device
    )
    DeviceList(devices)

}

@Composable
fun CustomDevicePage() {
    var ip by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("8880") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = ip,
                maxLines = 1,
                onValueChange = { value ->
                    ip = value
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.field_ip_address)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = port,
                maxLines = 1,
                onValueChange = { value ->
                    port = value
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
                onClick = {
                    Toast.makeText(mainActivity, "Checking Connection", Toast.LENGTH_SHORT).show()
                    // Launch a coroutine on the IO dispatcher for network operations
                    CoroutineScope(Dispatchers.IO).launch {
                        var success = false
                        try {
                            success = isFoobarServer(ip, port.toInt(), 1000)
                        } catch (e: Exception) {
                            success = false
                        }

                        // Switch back to the main thread to show the Toast messages
                        withContext(Dispatchers.Main) {
                            if (success) {
                                Toast.makeText(mainActivity, "Connection successful", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(mainActivity, "Connection failed", Toast.LENGTH_SHORT).show()
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
                onClick = {
                    saveIpAddress("$ip:$port", mainActivity)
                    mainActivity.navigateTo("Settings")
                },
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
    CustomDevicePage()
}

@Preview
@Composable
fun DeviceSelectionPagePreview(){
    DeviceSelectionPage()
}