package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

enum class LayoutItems(val text : String, val onPlayer: Boolean = true, val onAlbum: Boolean = true, val onTitle: Boolean = true) {
    CATALOG(mainActivity.baseContext.getString(R.string.layout_item_medium)),
    COMPOSER(mainActivity.baseContext.getString(R.string.layout_item_composer)),
    ALBUM(mainActivity.baseContext.getString(R.string.layout_item_album)),
    TITLE(mainActivity.baseContext.getString(R.string.layout_item_title), onAlbum = false),
    ARTIST(mainActivity.baseContext.getString(R.string.layout_item_artist)),
    SMART_ARTIST(mainActivity.baseContext.getString(R.string.layout_item_smart_artist), onAlbum = false, onPlayer = false),
    PROGRESS(mainActivity.baseContext.getString(R.string.layout_item_progress_bar), onTitle = false, onAlbum = false),
    ARTWORK(mainActivity.baseContext.getString(R.string.layout_item_cover), onTitle = false, onAlbum = false),
    UNDEFINED("UNDEFINED", onTitle = false, onAlbum = false, onPlayer = false);

    fun getLayoutItemsFor(viewWithLayout: ViewsWithLayout):List<LayoutItems> {
        return when(viewWithLayout){
            ViewsWithLayout.PLAYER -> {
                entries.filter( { it.onPlayer })
            }

            ViewsWithLayout.ALBUM -> {
                entries.filter( { it.onAlbum })
            }

            ViewsWithLayout.TITLE -> {
                entries.filter( { it.onTitle })
            }
        }
    }
}

