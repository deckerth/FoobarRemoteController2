package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import kotlin.reflect.KProperty

class LayoutDescription() {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): LayoutDescription {
        return this
    }

    var items = mutableListOf<LayoutItem>()
}
