package com.deckerth.thomas.foobarremotecontroller2.model

data class CustomField(val fieldReference: String, val fieldName: String)

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

    //todo: read from settings
    init {
        addCustomField("contributor", "Contributor")
        }

    fun addCustomField(fieldReference: String, fieldName: String) {
        customFields[fieldReference] = CustomField(fieldReference, fieldName)
        customFieldIndex.add(fieldReference)
    }

    fun iaStandardField(fieldReference: String): Boolean {
        return standardFields.contains(fieldReference)
    }

    fun get(index: Int): CustomField? {
        return if (index >= customFields.size) null else customFields[customFieldIndex[index]]
    }

    fun get(fieldReference: String): CustomField? {
        return customFields[fieldReference]
    }

    fun getEscapedColumnList(): String {
        var columnList = ""
        for (i in 0 until customFieldIndex.size) {
            columnList += ",%25" + customFields[customFieldIndex[i]]!!.fieldReference + "%25"
        }
        return columnList
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
            customFieldsContent[field.fieldReference] = value
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

    fun getText(fieldReference: String): String {
        return customFieldsContent[fieldReference] ?: ""
    }
}

