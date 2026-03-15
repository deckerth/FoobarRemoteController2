package com.deckerth.thomas.foobarremotecontroller2.model

import androidx.compose.runtime.mutableStateOf
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.saveCustomFields
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import kotlinx.serialization.Serializable
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Serializable
data class CustomField(var fieldReference: String, val fieldName: String)
@Serializable
data class CustomFieldList(val customFields: List<CustomField>) // for settings

// Columns:
//    1 %25label%25,
//    2 %25catalog%25,
//    3 %25composer%25,
//    4 %25album%25,
//    5 %25title%25,
//    6 %25artist%25,
//    7 %25album artist%25,
//    8 %25samplerate%25,
//    9 %25genre%25,
//    10 %25discnumber%25,
//    11 %25track%25,
//    12 %25playback_time%25,
//    13 %25length_seconds_fp%25,
//    14 %24filename%28%25path%25%29%24&" becomes:$filename(%path%) / %25path%25%

val standardFields: List<String> = listOf(
    "%label%", "%catalog%", "%composer%", "%album%", "%title%", "%artist%", "%album artist%",
    "%samplerate%", "%genre%", "%discnumber%", "%track%", "%playback_time%", "%length_seconds_fp%",
    "%filename%", "%path%"
)

class CustomFields {
    val customFields = mutableMapOf<String, CustomField>()
    val customFieldIndex = mutableListOf<String>()

    val customFieldsList = mutableStateOf(CustomFieldList(listOf()))

    fun setCustomFields(customFieldList: CustomFieldList) {
        customFields.clear()
        customFieldIndex.clear()
        for (customField in customFieldList.customFields)
            addCustomField(customField.fieldReference, customField.fieldName, updateList = false)
        initCustomFieldList()
    }

    private fun initCustomFieldList() {
        customFieldsList.value = getCustomFields()
    }

    private fun getCustomFields() : CustomFieldList {
        val customFieldList = mutableListOf<CustomField>()
        for (customField in customFields) {
            customFieldList.add(customField.value)
        }
        return CustomFieldList(customFieldList)
    }

    fun getPreferenceSummary(): String {
        if (customFields.isEmpty()) return mainActivity!!.getString(R.string.no_custom_fields_defined)
        var summary = ""
        for (i in 0 until customFields.size) {
            if (i > 0) summary += ", "
            summary += customFields[customFieldIndex[i]]!!.fieldName
        }
        return summary
    }

    private fun convertToReference(fieldReference: String): String {
        var correctedFieldRef = fieldReference.trim()
        if (!correctedFieldRef.startsWith("$")) {
            if (!correctedFieldRef.startsWith("%"))
                correctedFieldRef = "%$correctedFieldRef"
            if (!correctedFieldRef.endsWith("%")) correctedFieldRef = "${correctedFieldRef}%"
        }
        return correctedFieldRef
    }

    fun addCustomField(fieldReference: String, fieldName: String, updateList: Boolean = true) {
        val correctedFieldRef = convertToReference(fieldReference)
        if (customFields.containsKey(fieldName)) return
        customFields[fieldName] = CustomField(correctedFieldRef, fieldName)
        customFieldIndex.add(fieldName)
        if (updateList) initCustomFieldList()
    }

    fun setFieldReference(fieldName: String, fieldReference: String) {
        if (!customFields.containsKey(fieldName)) return
        val correctedFieldRef = convertToReference(fieldReference)
        customFields[fieldName]!!.fieldReference = correctedFieldRef
    }

    fun removeCustomField(fieldName: String) {
        customFields.remove(fieldName)
        customFieldIndex.remove(fieldName)
        initCustomFieldList()
    }

    fun saveCustomFieldsSetting() {
        val customFieldList = getCustomFields()
        saveCustomFields(mainActivity!!, customFieldList)
    }

    fun iaStandardField(fieldReference: String): Boolean {
        return standardFields.contains(convertToReference(fieldReference))
    }

    fun hasField(fieldName: String): Boolean {
        return customFields.containsKey(fieldName)
    }

    fun get(index: Int): CustomField? {
        return if (index >= customFields.size) null else customFields[customFieldIndex[index]]
    }

    fun get(fieldName: String): CustomField? {
        return customFields[fieldName]
    }

    fun getEncodedColumnList(): String {
        var columnList = ""
        for (i in 0 until customFieldIndex.size) {
            columnList += "," + customFields[customFieldIndex[i]]!!.fieldReference
        }
        return URLEncoder.encode(columnList, StandardCharsets.UTF_8.toString())
    }

    fun getMockedContent(): CustomFieldsContent {
        val content = CustomFieldsContent()
        for ((_, value) in customFields) {
            content.setCustomFieldContent(value, value.fieldName)
        }
        return content
    }
}

class CustomFieldsContent {
    val customFieldsContent = mutableMapOf<String, String>()

    fun setCustomFieldContent(field: CustomField?, value: String) {
        if (field != null) {
            customFieldsContent[field.fieldName] = value
        }
    }

    fun matches(upperPattern: String): Boolean {
        for (field in customFieldsContent) {
            if (field.value.contains(upperPattern)) {
                return true
            }
        }
        return false
    }

    fun getContent(fieldName: String): String {
        return customFieldsContent[fieldName] ?: ""
    }
}

