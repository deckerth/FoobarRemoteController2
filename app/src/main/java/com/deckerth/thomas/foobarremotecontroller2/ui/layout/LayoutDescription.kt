package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import kotlinx.serialization.Serializable
import kotlin.reflect.KProperty

@Serializable
data class LayoutDescription(val view: ViewsWithLayout) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): LayoutDescription {
        return this
    }

    var items = mutableListOf<LayoutItem>()
}
