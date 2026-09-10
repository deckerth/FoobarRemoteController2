package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import com.deckerth.thomas.foobarremotecontroller2.R
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity
import com.deckerth.thomas.foobarremotecontroller2.viewmodel.AppViewModel

/**
Required steps when adding new metadata fields:

1. add the new field to the enum
2. add the field to the ITitle interface
3. adapt the Title class accordingly (also matches())
4. add the field to PlayerViewModel
5. adjust the classes QueryAccess, PlayerAccess (in particular columnsWithPath..) and PlaylistAccess.
   Pay attention to computation of effectiveTitle
   Adjust DisplayItemDetail (TitleDetails)
6. extend LayoutComponent
7. adapt the example objects in LayoutPreviewPage
8. extend TitleDetails with the new field (DisplayItemDetail)
 */
enum class StandardLayoutItems(
    val text: String,
    val onPlayer: Boolean = true,
    val onAlbum: Boolean = true,
    val onTitle: Boolean = true
) {
    LABEL(mainActivity!!.baseContext.getString(R.string.layout_item_label)),
    CATALOG(mainActivity!!.baseContext.getString(R.string.layout_item_medium)),
    LABEL_CATALOG(mainActivity!!.baseContext.getString(R.string.layout_item_label_catalog)),
    COMPOSER(mainActivity!!.baseContext.getString(R.string.layout_item_composer)),
    ALBUM(mainActivity!!.baseContext.getString(R.string.layout_item_album)),
    TITLE(mainActivity!!.baseContext.getString(R.string.layout_item_title), onAlbum = false),
    ARTIST(mainActivity!!.baseContext.getString(R.string.layout_item_artist)),
    ARTIST_TITLE(mainActivity!!.baseContext.getString(R.string.layout_item_artist_title)),

    GENRE(mainActivity!!.baseContext.getString(R.string.layout_item_genre)),
    SMART_ARTIST(
        mainActivity!!.baseContext.getString(R.string.layout_item_smart_artist),
        onAlbum = false,
        onPlayer = false
    ),
    ALBUM_ARTIST(mainActivity!!.baseContext.getString(R.string.layout_item_album_artist)),
    PROGRESS(
        mainActivity!!.baseContext.getString(R.string.layout_item_progress_bar)
    ),
    ARTWORK(
        mainActivity!!.baseContext.getString(R.string.layout_item_cover),
        onTitle = false,
        onAlbum = false,
        onPlayer = false
    ),
    SAMPLE_RATE(mainActivity!!.baseContext.getString(R.string.layout_item_samplerate)),

    TRACK(mainActivity!!.baseContext.getString(R.string.layout_item_track)),
    DURATION(mainActivity!!.baseContext.getString(R.string.layout_item_duration)),
    PATH(mainActivity!!.baseContext.getString(R.string.layout_item_path)),

    CUSTOM_FIELD("CUSTOM_FIELD", onTitle = false, onAlbum = false, onPlayer = false),
    UNDEFINED("UNDEFINED", onTitle = false, onAlbum = false, onPlayer = false)
}

class LayoutItems(
    val item: StandardLayoutItems,
    val customFieldName: String,
    val text: String
)

fun getLayoutItemsFor(vm: AppViewModel, viewWithLayout: ViewsWithLayout): List<LayoutItems> {
    val entries = mutableListOf<LayoutItems>()

    for (item in StandardLayoutItems.entries) {
        if (item != StandardLayoutItems.CUSTOM_FIELD) {
            if (viewWithLayout == ViewsWithLayout.PLAYER && !item.onPlayer)
                continue
            if (viewWithLayout == ViewsWithLayout.ALBUM && !item.onAlbum)
                continue
            if (viewWithLayout == ViewsWithLayout.TITLE && !item.onTitle)
                continue

             entries.add(
                LayoutItems(
                    item,
                    "",
                    item.text))
        }
    }

    for (field in vm.customFields.customFields) {
        entries.add(
            LayoutItems(
                StandardLayoutItems.CUSTOM_FIELD,
                field.value.fieldName,
                field.value.fieldName))
    }

    return entries
}

