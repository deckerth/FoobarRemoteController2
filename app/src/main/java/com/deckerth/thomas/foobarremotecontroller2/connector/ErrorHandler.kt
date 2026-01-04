package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Timestamp

enum class ErrorType {
    NETWORK,
    API,
    UNKNOWN
}

enum class ErrorCode {
    NO_INTERNET_CONNECTION,
    BAD_RESPONSE,
    CONNECTION_ERROR,
    AUTHORIZATION_ERROR,
    UNKNOWN_ERROR
}

enum class ErrorSource {
    PLAYER_STATE,
    PLAYLISTS,
    PLAYLIST_ITEMS,
    HTTP_CONNECTOR,
    BROWSER
}

class ErrorHandler(private val vm: AppViewModel) {

    private var firstOccurrence: Timestamp? = null
    private var errorType: ErrorType = ErrorType.UNKNOWN
    private var errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR
    private var errorSource: ErrorSource = ErrorSource.PLAYER_STATE
    var authorizationErrorOccurred = false
    private var healing = false

    fun logError(
        errorType: ErrorType,
        errorCode: ErrorCode,
        errorSource: ErrorSource,
        error: Exception
    ) {
        this.errorType = errorType
        this.errorCode = errorCode
        this.errorSource = errorSource

        if (firstOccurrence == null)
            firstOccurrence = Timestamp(System.currentTimeMillis())

        if (errorCode == ErrorCode.AUTHORIZATION_ERROR)
            authorizationErrorOccurred = true
        var message = ""
        if (error.message != null) message = error.message!!

        println("FOOB Error type: $errorType, Error code: $errorCode, Error source: $errorSource, Error: $message")
    }

    fun sick(): Boolean {
        vm.isSick =
            (firstOccurrence != null) && ((System.currentTimeMillis() - firstOccurrence!!.time) > 5000)
        if (vm.isSick && !healing) {
            healing = true
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    while (true) {
                        println("FOOB Healing")
                        delay(10000L)
                        if (!vm.askForPassword) {
                            withContext(Dispatchers.Main) { reset() }
                            println("FOOB Healing finished")
                            break
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
        return vm.isSick
    }

    fun reset() {
        healing = false
        authorizationErrorOccurred = false
        firstOccurrence = null
    }

}
