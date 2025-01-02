package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.isSick
import java.sql.Timestamp

val errorHandler = ErrorHandler()

enum class ErrorType {
    NETWORK,
    API,
    UNKNOWN
}

enum class ErrorCode {
    NO_INTERNET_CONNECTION,
    BAD_RESPONSE,
    CONNECTION_ERROR,
    UNKNOWN_ERROR
}

enum class ErrorSource {
    PLAYER_STATE,
    PLAYLISTS,
    PLAYLIST_ITEMS,
    HTTP_CONNECTOR,
    BROWSER
}

class ErrorHandler {

    private var firstOccurrence: Timestamp? = null
    private var errorType: ErrorType = ErrorType.UNKNOWN
    private var errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR
    private var errorSource: ErrorSource = ErrorSource.PLAYER_STATE
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

        var message = ""
        if (error.message != null) message = error.message!!

        println("FOOB Error type: $errorType, Error code: $errorCode, Error source: $errorSource, Error: $message")
    }

    fun sick(): Boolean {
        isSick =
            (firstOccurrence != null) && ((System.currentTimeMillis() - firstOccurrence!!.time) > 5000)
        if (isSick && !healing) {
            healing = true
            Thread {
                try {
                    println("FOOB Healing")
                    Thread.sleep(10000)
                    reset()
                    println("FOOB Healing finished")
                } catch (_: Exception) {
                }
            }.start()
        }
        return isSick
    }

    fun reset() {
        healing = false
        firstOccurrence = null
    }

}