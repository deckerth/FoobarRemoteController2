package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.model.Playlist
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.displayedPlaylist
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.getPlaylistToBeUpdated
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.isSick
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.loadingList
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.selectedPlaylist
import java.sql.Timestamp

public val errorHandler = ErrorHandler()

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
    HTTP_CONNECTOR
}

class ErrorHandler {

    private var firstOccurrance: Timestamp? = null
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

        if (firstOccurrance == null)
            firstOccurrance = Timestamp(System.currentTimeMillis())

        var message = ""
        if (error.message != null) message = error.message!!

        println("FOOB Error type: $errorType, Error code: $errorCode, Error source: $errorSource, Error: $message")
    }

    fun sick(): Boolean {
        isSick = (firstOccurrance != null) && ((System.currentTimeMillis() - firstOccurrance!!.time) > 5000)
        if (isSick && !healing) {
            healing = true
            Thread {
                try {
                    println("FOOB Healing")
                    Thread.sleep(10000)
                    reset()
                    println("FOOB Healing finished")
                } catch (_: Exception){  }
            }.start()
        }
        return isSick
    }

    fun reset() {
        healing = false
        firstOccurrance = null
    }

}