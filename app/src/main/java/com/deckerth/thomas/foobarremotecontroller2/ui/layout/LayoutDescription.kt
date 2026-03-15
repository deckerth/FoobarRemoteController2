package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty

@Serializable
data class LayoutDescription(val view: ViewsWithLayout) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): LayoutDescription {
        return this
    }

    var items = mutableListOf<LayoutItem>()

    fun getLayoutItems(): MutableList<LayoutItems> {
        val layoutItems = mutableListOf<LayoutItems>()
        for (item in items) {
            layoutItems.add(LayoutItems(item.item, item.customFieldName, item.getText()))
        }
        return layoutItems
    }

    fun isUsedInLayout(fieldName : String) : Boolean {
        return items.any { it.customFieldName == fieldName }
    }

    fun removeFieldFromLayout(fieldName : String) {
        items.removeIf { it.customFieldName == fieldName }
    }

    fun customFieldsAdded(other : LayoutDescription) : Boolean {
        return items.any { it.item == StandardLayoutItems.CUSTOM_FIELD && !other.items.contains(it) }
    }

}
