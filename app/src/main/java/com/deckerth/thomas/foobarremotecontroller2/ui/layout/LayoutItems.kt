package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.layout.LayoutItems.entries
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

/**
Required steps when adding new metadata fields:

1. add the new field to the enum
2. add the field to the ITitle interface
3. adapt the Title class accordingly
4. add the field to Player
5. adjust the classes PlayerAccess and PlaylistAccess
6. extend LayoutComponent
7. adapt the example objects in LayoutPreviewPage
8. extend TitleDetails with the new field
*/
enum class LayoutItems(val text : String, val onPlayer: Boolean = true, val onAlbum: Boolean = true, val onTitle: Boolean = true) {
    LABEL(mainActivity.baseContext.getString(R.string.layout_item_label)),
    CATALOG(mainActivity.baseContext.getString(R.string.layout_item_medium)),
    LABEL_CATALOG(mainActivity.baseContext.getString(R.string.layout_item_label_catalog)),
    COMPOSER(mainActivity.baseContext.getString(R.string.layout_item_composer)),
    ALBUM(mainActivity.baseContext.getString(R.string.layout_item_album)),
    TITLE(mainActivity.baseContext.getString(R.string.layout_item_title), onAlbum = false),
    ARTIST(mainActivity.baseContext.getString(R.string.layout_item_artist)),
    SMART_ARTIST(mainActivity.baseContext.getString(R.string.layout_item_smart_artist), onAlbum = false, onPlayer = false),
    PROGRESS(mainActivity.baseContext.getString(R.string.layout_item_progress_bar), onTitle = false, onAlbum = false),
    ARTWORK(mainActivity.baseContext.getString(R.string.layout_item_cover), onTitle = false, onAlbum = false, onPlayer = false),
    UNDEFINED("UNDEFINED", onTitle = false, onAlbum = false, onPlayer = false);
}

fun getLayoutItemsFor(viewWithLayout: ViewsWithLayout):List<LayoutItems> {
    return when(viewWithLayout){
        ViewsWithLayout.PLAYER -> {
            entries.filter { it.onPlayer }
        }

        ViewsWithLayout.ALBUM -> {
            entries.filter { it.onAlbum }
        }

        ViewsWithLayout.TITLE -> {
            entries.filter { it.onTitle }
        }
        else -> {
            emptyList()
        }
    }
}

