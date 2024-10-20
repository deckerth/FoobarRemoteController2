package com.deckerth.thomas.foobarremotecontroller2.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.saveIpAddress
import com.deckerth.thomas.foobarremotecontroller2.ui.theme.Foobar2000RemoteControllerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSelectionPage(){
    devices.clear()
    Thread{
        runBlocking {
            scanForFoobarServers(8880)
        }
    }.start()
    Column{
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        DeviceList(devices = devices)
    }

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
    return try {
        val url = URL("http://$ip:$port/api/player")
        with(url.openConnection() as HttpURLConnection) {
            connectTimeout = timeout
            readTimeout = timeout
            requestMethod = "GET"

            connect()
            responseCode == 200
        }
    } catch (e: Exception) {
        false
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
                    saveIpAddress(device.ipAddress, mainActivity)
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

@Preview
@Composable
fun DeviceSelectionPagePreview(){
    DeviceSelectionPage()
}