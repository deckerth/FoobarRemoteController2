package com.deckerth.thomas.foobarremotecontroller2.connector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.deckerth.thomas.foobarremotecontroller2.model.checkIpAddressSyntax
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class Response(val usedIpAddress: String, var message: String = "")

class HTTPConnector(private val vm: AppViewModel) {
    fun serverAddress(ipAddress: String?): String {
        if (ipAddress != null) {
            return "http://${ipAddress}/api/"
        }
        return ""
    }

    fun getData(endpoint: String): Response {
        // Fetch data from the API in the background.
        val response = Response(vm.ipAddress!!)
        val result = StringBuilder()
        try {
            val url: URL
            var urlConnection: HttpURLConnection? = null
            try {
                url = URL(serverAddress(response.usedIpAddress) + endpoint)
                //open a URL connection
                urlConnection = url.openConnection() as HttpURLConnection
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
            } catch (e: Exception) {
                e.printStackTrace()
                vm.errorHandler.logError(
                    ErrorType.NETWORK,
                    ErrorCode.CONNECTION_ERROR,
                    ErrorSource.HTTP_CONNECTOR,
                    e
                )
                throw e
            } finally {
                urlConnection?.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            vm.errorHandler.logError(
                ErrorType.NETWORK,
                ErrorCode.CONNECTION_ERROR,
                ErrorSource.HTTP_CONNECTOR,
                e
            )
            throw e
        }
    }

    fun checkConnection(ip: String): Boolean {
        if (!checkIpAddressSyntax(ip)) return false
        try {
            val url = URL("http://$ip/api/playlists")
            println("FOOB \"http://$ip/api/playlists\"")
            //open a URL connection
            val urlConnection = url.openConnection() as HttpURLConnection
            val response = urlConnection.responseCode
            val message = urlConnection.responseMessage
            println("FOOB response: $response message:$message")
            return response == 200 && message == "OK"
        } catch (e: Exception) {
            e.printStackTrace()
            return false
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

    fun postData(endpoint: String) {
        val result = StringBuilder()
        try {
            val url: URL
            var urlConnection: HttpURLConnection? = null
            try {
                url = URL(serverAddress(vm.ipAddress!!) + endpoint)
                //open an URL connection
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true
                urlConnection.setRequestProperty("Accept", "*/*")
                urlConnection.connect()
                val `in` = urlConnection.inputStream
                val isw = InputStreamReader(`in`)
                var data = isw.read()
                while (data != -1) {
                    result.append(data.toChar())
                    data = isw.read()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun postData(endpoint: String, data: String) {
        val result = StringBuilder()
        try {
            val url: URL
            var urlConnection: HttpURLConnection? = null
            try {
                url = URL(serverAddress(vm.ipAddress!!) + endpoint)
                //open a URL connection
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.doOutput = true
                //urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                urlConnection.connect()
                urlConnection.outputStream.write(data.toByteArray())
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
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return if (!(src!!.contains("-1")))
            try {
                println("FOOB getBitmapFromURL: $src")
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        else
            null
    }
}
