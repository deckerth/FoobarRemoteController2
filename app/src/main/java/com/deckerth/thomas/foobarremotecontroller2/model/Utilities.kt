package com.deckerth.thomas.foobarremotecontroller2.model

fun checkIpAddressSyntax(ipAddress: String): Boolean {
    val ipRegex = Regex("""^(\d{1,3}\.){3}\d{1,3}\:\d{1,5}${'$'}""")
    return ipRegex.matches(ipAddress)
}

fun checkPortSyntax(port: String): Boolean {
    return port.toIntOrNull() in 1..65535
}

fun checkIpSyntax(ipAddress: String): Boolean {
    val ipRegex = Regex("""^(\d{1,3}\.){3}\d{1,3}""")
    return ipRegex.matches(ipAddress)
}