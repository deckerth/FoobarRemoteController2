package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutDescription
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getLayoutItemsFor
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

data class LayoutField(
    val key: Int,
    val layoutItem: LayoutItem? = null,
    val sectionTitle: String = "",
    val isSectionTitle: Boolean = false,
)

class LayoutViewModel(private val vm: AppViewModel) : ViewModel() {
    var layoutFields = mutableStateOf<List<LayoutField>>(emptyList())

    var dirty = mutableStateOf(false)

    var currentView: ViewsWithLayout = ViewsWithLayout.UNDEFINED
        set(value) {
            if (value == field) return
            field = value
            initializeViewModel()
        }
    private var layoutDescription: LayoutDescription? = null

    fun saveChanges() {
        if (dirty.value) {
            var sectionCount = 0
            val description = LayoutDescription(currentView)
            for (field in layoutFields.value) {
                if (field.isSectionTitle) {
                    sectionCount++
                    if (sectionCount == 2)
                        break
                } else
                    if (field.layoutItem != null) description.items.add(field.layoutItem)
            }
            layoutManager.setCustomLayoutDescription(vm.selectedView, description)
            dirty.value = false
        }
    }

    private fun initializeViewModel() {
        layoutDescription = layoutManager.getCustomLayoutDescription(vm.selectedView)

        val layoutItems = layoutDescription!!.items
        val allItems = getLayoutItemsFor(vm.selectedView)
        val unusedItems = allItems.filter { item -> !layoutItems.any { it.item == item } }
        val fields = mutableListOf<LayoutField>()

        fields.add(LayoutField(0, null, vm.selectedView.getText(vm.selectedView), true))
        var i = 1
        for (item in layoutItems) {
            if (item.item == LayoutItems.ARTWORK)
                continue
            fields.add(LayoutField(i, item))
            i++
        }
        fields.add(LayoutField(i, null, mainActivity!!.getString(R.string.available_fields), true))
        i++
        for (item in unusedItems) {
            fields.add(
                LayoutField(
                    i,
                    LayoutItem(
                        item,
                        itemSize = if (item == LayoutItems.ARTWORK) ItemSize.MEDIUM_COVER else ItemSize.BODY_MEDIUM
                    )
                )
            )
            i++
        }
        layoutFields.value = fields
    }

    class Factory(private val vm: AppViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Check if the requested ViewModel is LayoutViewModel
            if (modelClass.isAssignableFrom(LayoutViewModel::class.java))
            // Create and return an instance of MyViewModel with the dependency
            {
                // Create and return an instance of MyViewModel with the dependency
                @Suppress("UNCHECKED_CAST")
                return LayoutViewModel(vm) as T
            }
            // If the requested ViewModel is not LayoutViewModel, throw an exception
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}