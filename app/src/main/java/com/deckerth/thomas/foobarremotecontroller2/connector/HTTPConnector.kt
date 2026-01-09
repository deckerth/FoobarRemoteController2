package com.deckerth.thomas.foobarremotecontroller2.connector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.deckerth.thomas.foobarremotecontroller2.model.checkIpAddressSyntax
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

const val NO_CONNECTION = 400
const val NOT_AUTHORIZED = 401
const val CONNECTED = 200

data class Response(
    val usedIpAddress: String,
    var message: String = ""
)

open class HTTPConnector() {
    fun serverAddress(ipAddress: String?): String {
        if (ipAddress != null) {
            return "http://${ipAddress}/api/"
        }
        return ""
    }

    fun basicAuthHeader(username: String, password: String): String {
        val credentials = "$username:$password"
        val base64Credentials = Base64.getEncoder().encodeToString(credentials.toByteArray())
        return "Basic $base64Credentials"
    }

    fun setCredentials(urlConnection: HttpURLConnection, vm: AppViewModel) {
        val user = vm.credentialsManager.getUser()
        val password = vm.credentialsManager.getPassword()
        if (user.isNotBlank()) {
            val authHeader = basicAuthHeader(user, password)
            urlConnection.setRequestProperty("Authorization", authHeader)
        }
    }

    fun getData(endpoint: String, vm: AppViewModel): Response {
        // Fetch data from the API in the background.
        val response = Response(vm.ipAddress!!)
        val result = StringBuilder()

        var url: URL
        var urlConnection: HttpURLConnection? = null
        try {
            url = URL(serverAddress(response.usedIpAddress) + endpoint)
            //open a URL connection
            urlConnection = url.openConnection() as HttpURLConnection
            setCredentials(urlConnection, vm)

            val connectionResponse = urlConnection.responseCode  // 401 -> Unauthorized, 200 -> OK
            println("FOOB response (getData): $connectionResponse")
            if (connectionResponse == NOT_AUTHORIZED) {
                throw IOException("Unauthorized")
            } else {
                // Uncommenting the following can cause issues
                println("FOOB $url ")

                val inputStream = urlConnection.inputStream
                val isw = InputStreamReader(inputStream)
                var data = isw.read()
                while (data != -1) {
                    result.append(data.toChar())
                    data = isw.read()

                }
                vm.errorHandler.reset()
                // return the data to onPostExecute method
                response.message = result.toString()
                return response
            }
        } catch (e: Exception) {
            if (e.message != null && e.message.equals("Unauthorized"))
                vm.errorHandler.logError(
                    ErrorType.NETWORK,
                    ErrorCode.AUTHORIZATION_ERROR,
                    ErrorSource.HTTP_CONNECTOR,
                    e
                )
            else {
                e.printStackTrace()
                vm.errorHandler.logError(
                    ErrorType.NETWORK,
                    ErrorCode.CONNECTION_ERROR,
                    ErrorSource.HTTP_CONNECTOR,
                    e
                )
            }
            throw e
        } finally {
            urlConnection?.disconnect()
        }
    }

    fun checkConnection(ip: String, vm: AppViewModel): Int {
        //vm.credentialsManager.setAuthenticator()
        if (!checkIpAddressSyntax(ip)) return NO_CONNECTION
        try {
            val url = URL("http://$ip/api/playlists")
            println("FOOB checkConnection \"http://$ip/api/playlists\"")
            //open a URL connection
            val urlConnection = url.openConnection() as HttpURLConnection
            setCredentials(urlConnection, vm)
            val response = urlConnection.responseCode  // 401 -> Unauthorized, 200 -> OK
            val message = urlConnection.responseMessage
            urlConnection.disconnect()
            println("FOOB response: $response message:$message")
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return NO_CONNECTION
        }
    }

    /*
        fun getImage(endpoint: String): Bitmap? {
            // Fetch data from the API in the background.
            val result: Bitmap
            try {
                val url: URL
                var urlConnection: HttpURLConnection? = null
                try {
                    url = URL(serverAddress + endpoint)
                    //open a URL connection
                    urlConnection = url.openConnection() as HttpURLConnection
                    val `in` = urlConnection.inputStream

                    result = BitmapFactory.decodeStream(`in`)

                    // return the data to onPostExecute method
                    return result
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    urlConnection?.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return null
        }
    */

    fun postData(endpoint: String, vm: AppViewModel) {
        val result = StringBuilder()
        //vm.credentialsManager.setAuthenticator()
        var url: URL
        var urlConnection: HttpURLConnection? = null
        try {
            url = URL(serverAddress(vm.ipAddress!!) + endpoint)
            //open an URL connection
            urlConnection = url.openConnection() as HttpURLConnection
            setCredentials(urlConnection, vm)
            urlConnection.requestMethod = "POST"
            urlConnection.doOutput = true
            urlConnection.setRequestProperty("Accept", "*/*")
            val response = urlConnection.responseCode  // 401 -> Unauthorized, 200 -> OK
            println("FOOB response (postData): $response")
            if  (response in 200..299) { // // Check for a successful response range
                val `in` = urlConnection.inputStream
                val isw = InputStreamReader(`in`)
                var data = isw.read()
                while (data != -1) {
                    result.append(data.toChar())
                    data = isw.read()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
        }
    }

    fun postData(endpoint: String, data: String, vm: AppViewModel) {
        val result = StringBuilder()
        //vm.credentialsManager.setAuthenticator()
        var url: URL
        var urlConnection: HttpURLConnection? = null
        try {
            url = URL(serverAddress(vm.ipAddress!!) + endpoint)
            //open a URL connection
            urlConnection = url.openConnection() as HttpURLConnection
            setCredentials(urlConnection, vm)
            urlConnection.requestMethod = "POST"
            urlConnection.doOutput = true
            urlConnection.setRequestProperty(
                "Content-Type",
                "application/json; charset=UTF-8"
            )
            urlConnection.outputStream.write(data.toByteArray())
            val response = urlConnection.responseCode  // 401 -> Unauthorized, 200 -> OK
            println("FOOB response (postData): $response")
            if (response in 200..299) { // Check for a successful response range
                try {  // try to read the response
                    val `in` = urlConnection.inputStream
                    val isw = InputStreamReader(`in`)
                    var responseData = isw.read()
                    while (responseData != -1) {
                        result.append(responseData.toChar())
                        responseData = isw.read()
                    }
                } catch (io: IOException) {
                    io.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
        }
    }

    fun getBitmapFromURL(src: String?, vm: AppViewModel): Bitmap? {
        var connection: HttpURLConnection? = null
        return if (!(src!!.contains("-1")))
            try {
                //vm.credentialsManager.setAuthenticator()
                println("FOOB getBitmapFromURL: $src")
                val url = URL(src)
                connection = url.openConnection() as HttpURLConnection
                setCredentials(connection, vm)
                val response = connection.responseCode  // 401 -> Unauthorized, 200 -> OK
                if (response in 200..299) {
                    val input = connection.inputStream
                    BitmapFactory.decodeStream(input)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                connection?.disconnect()
            }
        else
            null
    }
}
