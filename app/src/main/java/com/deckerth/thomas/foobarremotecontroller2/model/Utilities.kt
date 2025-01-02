package com.deckerth.thomas.foobarremotecontroller2.model

fun checkIpAddressSyntax(ipAddress: String): Boolean {
    val ipRegex = Regex("""^(\d{1,3}\.){3}\d{1,3}\:\d{1,5}${'$'}""")
    return ipRegex.matches(ipAddress)
}