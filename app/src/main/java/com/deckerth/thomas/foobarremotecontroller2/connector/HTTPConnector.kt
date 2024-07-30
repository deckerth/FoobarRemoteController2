package com.deckerth.thomas.foobarremotecontroller2.connector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.deckerth.thomas.foobarremotecontroller2.ip_address
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** @noinspection BlockingMethodInNonBlockingContext
 */
class HTTPConnector {
    val serverAddress: String
        get() {
            if (ip_address != null) {
                return "http://$ip_address:8880/api/"
            }
            return ""
        }

    fun getData(endpoint: String): String {
        // Fetch data from the API in the background.
        val result = StringBuilder()
        try {
            val url: URL
            var urlConnection: HttpURLConnection? = null
            try {
                url = URL(serverAddress + endpoint)
                //open a URL coonnection
                urlConnection = url.openConnection() as HttpURLConnection
                println("FOOB response:"+urlConnection.responseCode+" Message:"+urlConnection.responseMessage)
                val `in` = urlConnection.inputStream
                val isw = InputStreamReader(`in`)
                var data = isw.read()
                while (data != -1) {
                    result.append(data.toChar())
                    data = isw.read()
                }

                // return the data to onPostExecute method
                return result.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Exception: " + e.message
        }
        return result.toString()
    }

    fun checkConnection(ip:String): Boolean{
        try {
            val url = URL("http://$ip:8880/api/playlists")
            println("FOOB \"http://$ip:8880/api/playlists\"")
            //open a URL connection
            val urlConnection = url.openConnection() as HttpURLConnection
            val response = urlConnection.responseCode
            val message = urlConnection.responseMessage
            println("FOOB respone: $response message:$message")
            return response == 200 && message == "OK"
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

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

    fun postData(endpoint: String) {
        val result = StringBuilder()
        try {
            val url: URL
            var urlConnection: HttpURLConnection? = null
            try {
                url = URL(serverAddress + endpoint)
                //open a URL coonnection
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
                url = URL(serverAddress + endpoint)
                //open a URL coonnection
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
}
