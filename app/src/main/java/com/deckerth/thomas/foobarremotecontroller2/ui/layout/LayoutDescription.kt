package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel
import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty

@Serializable
data class LayoutDescription(val view: ViewsWithLayout) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): LayoutDescription {
        return this
    }

    var items = mutableListOf<LayoutItem>()

    fun getLayoutItems(vm: AppViewModel): MutableList<LayoutItems> {
        val layoutItems = mutableListOf<LayoutItems>()
        for (item in items) {
            layoutItems.add(LayoutItems(item.item, item.customFieldReference, item.getText(vm)))
        }
        return layoutItems
    }

}
