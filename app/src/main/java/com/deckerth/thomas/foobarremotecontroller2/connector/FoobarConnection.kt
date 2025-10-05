package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.saveFoobarConnections
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.serialization.Serializable

@Serializable
data class FoobarConnection(
    var ipAddress : String,
    var username : String,
    var password : String,
    var ivString : ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FoobarConnection

        if (ipAddress != other.ipAddress) return false
        if (username != other.username) return false
        if (password != other.password) return false
        if (!ivString.contentEquals(other.ivString)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ipAddress.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + ivString.contentHashCode()
        return result
    }
}

@Serializable
class ConnectionManager {
    private val connections: MutableMap<String, FoobarConnection> = mutableMapOf()

    fun addConnection(connection: FoobarConnection) {
        connections[connection.ipAddress] = connection
        saveFoobarConnections(mainActivity, this)
    }

    fun getConnection(ipAddress: String): FoobarConnection? {
        return connections[ipAddress]
    }

//    fun removeConnection(ipAddress: String) {
//        connections.remove(ipAddress)
//    }
//
//    fun listAllConnections(): List<FoobarConnection> {
//        return connections.values.toList()
//    }
}