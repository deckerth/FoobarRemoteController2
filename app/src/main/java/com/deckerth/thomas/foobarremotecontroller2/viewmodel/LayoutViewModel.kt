package com.deckerth.thomas.foobarremotecontroller2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ItemSize
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutDescription
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItem
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.StandardLayoutItems
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.ViewsWithLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.getLayoutItemsFor
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.layoutManager
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

data class LayoutField(
    val key: Int,
    var index: Int,
    val layoutItem: LayoutItem? = null,
    val sectionTitle: String = "",
    val isSectionTitle: Boolean = false,
    var isPartOfCurrentLayout: Boolean = false
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
    private var currentLayoutDescription: LayoutDescription? = null

    val history = mutableStateOf<List<LayoutDescription>>(emptyList())

    val previewPlayerClassic = PlayerViewModel()

    val previewPlayerPop = PlayerViewModel()

    var currentPlayer by mutableStateOf(previewPlayerPop)

    val impactedViews = mutableStateOf(listOf<ViewsWithLayout>())
    val indexOfFirstAvailableField = mutableIntStateOf(0)


    init {
        val customFields = vm.customFields.getMockedContent()
        previewPlayerClassic.update(
            "Decca",
            "421 670-2",
            "Puccini, Giacomo",
            "Tosca",
            "Act 1 - Scene 1 - \"Ah! Finalmente!\"",
            "Leontyne Price / Giuseppe di Stefano / Giuseppe Taddei / Carlo Cava / Fernando Corena / Piero de Palma / Leonardo Monreale / Alfredo Mariotti / Herbert Weiss / Wiener Staatsopernchor / Wiener Philharmoniker / Herbert von Karajan",
            "Price / Karajan",
            "44100",
            "Opera",
            "1",
            "1",
            "0:53",
            "p4",
            "1",
            "125.1700625",
            "53.58589853333333",
            R.drawable.cover_tosca.toString(),
            path = "M:\\music\\Decca\\421 670-2\\01.flac",
            PlaybackState.PLAYING,
            PlaybackMode.DEFAULT,
            "",
            false,
            customFields
        )

        previewPlayerPop.update(
            "Polydor",
            "517 007-2",
            "Björn Ulvaeus",
            "Gold - Greatest Hits",
            "Dancing Queen",
            "ABBA",
            "ABBA",
            "44100",
            "Pop",
            "1",
            "1",
            "0:50",
            "p4",
            "0",
            "232.2",
            "51.080651833333334",
            R.drawable.cover_abba.toString(),
            path = "M:\\music\\Polydor\\517 007-2\\01.flac",
            PlaybackState.PLAYING,
            PlaybackMode.DEFAULT,
            "",
            false,
            customFields
        )
    }

    fun saveChanges() {
        if (dirty.value) {
            val m = history.value.toMutableList()
            m.add(currentLayoutDescription!!)
            history.value = m
            var sectionCount = 0
            currentLayoutDescription = LayoutDescription(currentView)
            var isPartOfCurrentLayout = true
            var index = 0
            for (field in layoutFields.value) {
                field.index = index++
                if (field.isSectionTitle) {
                    sectionCount++
                    if (sectionCount == 2)
                        isPartOfCurrentLayout = false
                    indexOfFirstAvailableField.intValue = field.index+1
                } else {
                    field.isPartOfCurrentLayout = isPartOfCurrentLayout
                    if (isPartOfCurrentLayout && field.layoutItem != null) currentLayoutDescription!!.items.add(field.layoutItem)
                }
            }
            val old = layoutManager.getCustomLayoutDescription(vm.selectedView)
            if (currentLayoutDescription!!.customFieldsAdded(old)) {
                if (!impactedViews.value.contains(currentView)) impactedViews.value += currentView
            }
            layoutManager.setCustomLayoutDescription(vm.selectedView, currentLayoutDescription!!)
            dirty.value = false
        }
    }

    fun undo() {
        if (history.value.isNotEmpty()) {
            val m = history.value.toMutableList()
            currentLayoutDescription = m.removeAt(m.lastIndex)
            history.value = m
            layoutManager.setCustomLayoutDescription(
                vm.selectedView,
                currentLayoutDescription!!
            )
            dirty.value = false
            initializeViewModel()
        }
    }

    private fun initializeViewModel() {
        currentLayoutDescription = layoutManager.getCustomLayoutDescription(vm.selectedView)

        val layoutItems = currentLayoutDescription!!.items
        val allItems = getLayoutItemsFor(vm, vm.selectedView)
        val unusedItems = allItems.filter { item -> !layoutItems.any { it.item == item.item } }
        val fields = mutableListOf<LayoutField>()

        fields.add(LayoutField(0,0, null, vm.selectedView.getText(vm.selectedView), true))
        var i = 1
        for (item in layoutItems) {
            if (item.item == StandardLayoutItems.ARTWORK)
                continue
            fields.add(LayoutField(i, i,item, isPartOfCurrentLayout = true))
            i++
        }
        fields.add(LayoutField(i, i,null, mainActivity!!.getString(R.string.available_fields), true))
        i++
        indexOfFirstAvailableField.intValue = i
        for (item in unusedItems) {
            fields.add(
                LayoutField(
                    i,i,
                    LayoutItem(
                        item.item,
                        item.customFieldName,
                        itemSize = if (item.item == StandardLayoutItems.ARTWORK) ItemSize.MEDIUM_COVER else ItemSize.BODY_MEDIUM
                    ),
                    isPartOfCurrentLayout = false
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