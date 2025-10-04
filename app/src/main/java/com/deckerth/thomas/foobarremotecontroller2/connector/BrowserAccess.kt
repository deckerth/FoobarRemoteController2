package com.deckerth.thomas.foobarremotecontroller2.connector

import com.deckerth.thomas.foobarremotecontroller2.model.MusicDirectory
import com.deckerth.thomas.foobarremotecontroller2.model.MusicDirectoryEntry
import com.deckerth.thomas.foobarremotecontroller2.model.ParentDirectory
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BrowserAccess(private val vm: AppViewModel) {
    private var pathSeparator = "\\"

    fun getRoots(): MusicDirectory? {
        val response = queryRoots() ?: return null
        return parseRoots(response)
    }

    private fun parseRoots(response: String): MusicDirectory {
        val roots = MusicDirectory(vm, "ROOT", "", "NULL")
        try {
            val rootsObject = JSONObject(response)
            pathSeparator = rootsObject.getString("pathSeparator")
            val rootsArray = rootsObject.getJSONArray("roots")
            /*         {
                         "pathSeparator": "\\",
                         "roots": [
                         {
                             "name": "T:\\Music",
                             "path": "T:\\Music",
                             "size": -1,
                             "timestamp": 1735489572,
                             "type": "D"
                         }
                         ]
                     }
         */
            for (i in 0 until rootsArray.length()) {
                val rootObject = rootsArray.getJSONObject(i)
                roots.addEntry(
                    MusicDirectory(vm,
                        rootObject.getString("name"),
                        rootObject.getString("path"),
                        ""
                    )
                )
            }
        } catch (e: Exception) {
            vm.errorHandler.logError(
                ErrorType.API,
                ErrorCode.BAD_RESPONSE,
                ErrorSource.BROWSER,
                e
            )
        }
        return roots
    }

    private fun queryRoots(): String? {
        val response : Response?
        try {
            response = vm.connector.getData("browser/roots", vm)
        } catch (e: Exception) {
            vm.errorHandler.logError(
                ErrorType.NETWORK,
                ErrorCode.CONNECTION_ERROR,
                ErrorSource.BROWSER,
                e
            )
            return null
        }
        return response.message
    }

    private fun encodePath(path: String): String {
        return URLEncoder.encode(path, StandardCharsets.UTF_8.toString())
    }

    fun getDirectory(path: String, parentDirectory: String): MusicDirectory? {
        val encodedPath = encodePath(path)
        val response = queryDirectory(encodedPath) ?: return null
        return parseDirectory(path, parentDirectory, response)
    }

    private fun queryDirectory(encodedPath: String): String? {
        val response: Response?
        try {
            response = vm.connector.getData("browser/entries?path=$encodedPath", vm)
        } catch (e: Exception) {
            vm.errorHandler.logError(
                ErrorType.NETWORK,
                ErrorCode.CONNECTION_ERROR,
                ErrorSource.BROWSER,
                e
            )
            return null
        }
        return response.message
    }

    private fun parseDirectory(
        path: String,
        parentDirectory: String,
        response: String
    ): MusicDirectory {
        val entries = MusicDirectory(vm, path, path, parentDirectory)
        try {
            val entriesObject = JSONObject(response)
            val entriesArray = entriesObject.getJSONArray("entries")
            /*
                {
                    "entries": [
                        {
                            "name": "ALPHA 692",
                            "path": "T:\\Music\\Alpha\\ALPHA 692",
                            "size": -1,
                            "timestamp": 1687075150,
                            "type": "D"
                        },
                        {
                            "name": "02 Franz Joseph Haydn - Symphony no. 31 in D major _Mit dem Hornsignal__ II. Adagio.flac",
                            "path": "T:\\Music\\Alpha\\ALPHA 692\\02 Franz Joseph Haydn - Symphony no. 31 in D major _Mit dem Hornsignal__ II. Adagio.flac",
                            "size": 376391132,
                            "timestamp": 1687075094,
                            "type": "F"
                        }
                    ],
                    "pathSeparator": "\\"
                }

         */
            entries.addEntry(ParentDirectory(vm,"..", parentDirectory))
            for (i in 0 until entriesArray.length()) {
                val entryObject = entriesArray.getJSONObject(i)
                val type = entryObject.getString("type")
                entries.addEntry(
                    if (type == "D")
                        MusicDirectory(
                            vm,
                            entryObject.getString("name"),
                            entryObject.getString("path"),
                            path
                        )
                    else
                        MusicDirectoryEntry(
                            entryObject.getString("name"),
                            entryObject.getString("path")
                        )
                )
            }
        } catch (e: Exception) {
            vm.errorHandler.logError(
                ErrorType.API,
                ErrorCode.BAD_RESPONSE,
                ErrorSource.BROWSER,
                e
            )
        }
        return entries
    }

    fun escapePathSeparator(path: String): String {
        return path.replace(pathSeparator, pathSeparator + pathSeparator)
    }
}