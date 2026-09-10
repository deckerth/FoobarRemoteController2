package com.deckerth.thomas.foobarremotecontroller2.connector

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    private val traceFileName = "trace.log"
    private var firstOccurrence: Timestamp? = null
    private var errorType: ErrorType = ErrorType.UNKNOWN
    private var errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR
    private var errorSource: ErrorSource = ErrorSource.PLAYER_STATE
    var authorizationErrorOccurred = false
    private var healing = false

    fun writeTrace(message: String, withTimestamp: Boolean = true) {
        if (!vm.errorLogging) return
        if (mainActivity == null) return
        val context = mainActivity!!.baseContext
        try {
            val file = File(context.filesDir, traceFileName)
            if (withTimestamp) {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                file.appendText("$timestamp - $message\n")
            } else
                file.appendText("$message\n")
        } catch (_: Exception) {
        }
    }

    private fun getTraceFileUri(): Uri? {
        if (mainActivity == null) return null
        val context = mainActivity!!.baseContext
        val file = File(context.filesDir, traceFileName)
        if (!file.exists()) return null

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    fun shareTraceFile(context: Context) {
        writeTrace("--------------------------------------------------------------------------------", withTimestamp = false)
        writeTrace("${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }", withTimestamp = false)

        val uri = getTraceFileUri() ?: return

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(com.deckerth.thomas.foobarremotecontroller2.R.string.share_trace_file)
            )
        )
    }

    private fun deleteTraceFile(context: Context) {
        val file = File(context.filesDir, traceFileName)
        if (file.exists()) file.delete()
    }

    fun startNewTraceFile(context: Context) {
        deleteTraceFile(context)
        writeTrace("foobar Link Logfile", withTimestamp = false)
        writeTrace("--------------------------------------------------------------------------------", withTimestamp = false)
        writeTrace("${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }", withTimestamp = false)
        writeTrace("--------------------------------------------------------------------------------", withTimestamp = false)
    }

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

        logString("Error type: $errorType, Error code: $errorCode, Error source: $errorSource, Error: $message")
    }

    fun logString(message: String) {
        println("FOOB $message")
        writeTrace(message)
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
