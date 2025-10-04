package com.deckerth.thomas.foobarremotecontroller2.model

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun checkIpAddressSyntax(ipAddress: String): Boolean {
    val ipRegex = Regex("""^(\d{1,3}\.){3}\d{1,3}:\d{1,5}${'$'}""")
    val ok = ipRegex.matches(ipAddress)
    return ok
}

fun checkPortSyntax(port: String): Boolean {
    return port.toIntOrNull() in 1..65535
}

fun checkIpSyntax(ipAddress: String): Boolean {
    val ipRegex = Regex("""^(\d{1,3}\.){3}\d{1,3}""")
    return ipRegex.matches(ipAddress)
}

fun isWlanConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
}