package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import androidx.compose.runtime.Composable
import com.deckerth.thomas.foobarremotecontroller2.getCustomLayout
import com.deckerth.thomas.foobarremotecontroller2.getViewMode
import com.deckerth.thomas.foobarremotecontroller2.saveCustomLayout
import com.deckerth.thomas.foobarremotecontroller2.ui.isTablet
import com.deckerth.thomas.foobarremotecontroller2.ui.mainActivity

val layoutManager = LayoutManager()

class LayoutManager {

    private val classicLayout: Layout = createClassicLayout()
    private val modernLayout: Layout = createModernLayout()
    private var customLayout: Layout? = null

    @Composable
    fun InitLayoutManager() {
        // customLayout gets initialized when the player page opens
        // it may be null afterwards if the user has not saved a custom layout
        if (customLayout == null) customLayout = getCustomLayout()
    }

    private fun createModernLayout(): Layout {
        return Layout(
            playerLayout = createModernPlayerLayout(),
            albumLayout = createModernAlbumLayout(),
            titleLayout = createModernTitleLayout()
        )
    }

    private fun createModernPlayerLayout(): LayoutDescription {
        val fields = LayoutDescription(ViewsWithLayout.PLAYER)
        fields.items.add(LayoutItem(StandardLayoutItems.TITLE,  itemSize = ItemSize.TITLE_LARGE))
        fields.items.add(LayoutItem(StandardLayoutItems.ALBUM,  itemSize = ItemSize.BODY_SMALL))
        fields.items.add(LayoutItem(StandardLayoutItems.PROGRESS))
        return fields
    }

    private fun createModernAlbumLayout(): LayoutDescription {
        val fields = LayoutDescription(ViewsWithLayout.ALBUM)
        fields.items.add(LayoutItem(StandardLayoutItems.ALBUM, itemSize = ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(StandardLayoutItems.ARTIST, itemSize = ItemSize.BODY_SMALL))
        fields.items.add(LayoutItem(StandardLayoutItems.COMPOSER, itemSize = ItemSize.BODY_SMALL))
        return fields
    }

    private fun createModernTitleLayout(): LayoutDescription {
        val fields = LayoutDescription(ViewsWithLayout.TITLE)
        fields.items.add(LayoutItem(StandardLayoutItems.TITLE, itemSize = ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(StandardLayoutItems.SMART_ARTIST, itemSize = ItemSize.BODY_SMALL))
        if (!mainActivity!!.isTablet()) fields.items.add(LayoutItem(StandardLayoutItems.ALBUM, itemSize = ItemSize.BODY_SMALL))
        return fields
    }

    private fun createClassicLayout(): Layout {
        return Layout(
            playerLayout = createClassicPlayerLayout(),
            albumLayout = createClassicAlbumLayout(),
            titleLayout = createClassicTitleLayout()
        )
    }

    private fun createClassicPlayerLayout(): LayoutDescription {
        val fields = LayoutDescription(ViewsWithLayout.PLAYER)
        fields.items.add(LayoutItem(StandardLayoutItems.COMPOSER, itemSize = ItemSize.TITLE_LARGE))
        fields.items.add(
            LayoutItem(
                StandardLayoutItems.ALBUM,
                itemSize = ItemSize.TITLE_MEDIUM,
                italic = true
            )
        )
        fields.items.add(LayoutItem(StandardLayoutItems.TITLE, itemSize = ItemSize.BODY_SMALL))
        fields.items.add(LayoutItem(StandardLayoutItems.ARTIST, itemSize = ItemSize.BODY_SMALL, maxLines = 10))
        fields.items.add(LayoutItem(StandardLayoutItems.PROGRESS))
        return fields
    }

    private fun createClassicAlbumLayout(): LayoutDescription {
        val fields = LayoutDescription(ViewsWithLayout.ALBUM)
        fields.items.add(LayoutItem(StandardLayoutItems.COMPOSER, itemSize = ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(StandardLayoutItems.ALBUM, itemSize = ItemSize.BODY_MEDIUM))
        fields.items.add(LayoutItem(StandardLayoutItems.ARTIST, itemSize = ItemSize.BODY_SMALL))
        return fields
    }

    private fun createClassicTitleLayout(): LayoutDescription {
        val fields = LayoutDescription(ViewsWithLayout.TITLE)
        fields.items.add(LayoutItem(StandardLayoutItems.TITLE, itemSize = ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(StandardLayoutItems.SMART_ARTIST, itemSize = ItemSize.BODY_SMALL))
        if (!mainActivity!!.isTablet()) fields.items.add(LayoutItem(StandardLayoutItems.ALBUM, itemSize = ItemSize.BODY_SMALL))
        return fields
    }

    @Composable
    fun getLayout(requestedViewMode: Layouts? = null): Layout {
        val viewMode = requestedViewMode ?: getViewMode()

        println("FOOB getLayout: viewMode = $viewMode")

        return when (viewMode) {
            Layouts.LAYOUT_MODERN -> modernLayout
            Layouts.LAYOUT_CLASSIC -> classicLayout
            Layouts.LAYOUT_CUSTOM -> customLayout!!
            // customLayout gets initialized when the player page opens or,
            // if the user has not saved a custom layout, it gets initialized
            // when the user selects the custom layout in the settings page
        }
    }

    fun changeLayout(previousMode: Layouts, newLayout: Layouts) {
        if (newLayout == Layouts.LAYOUT_CUSTOM) {
            if (customLayout == null) {
                // it is the first time the user has selected the custom layout
                customLayout = when (previousMode) {
                    Layouts.LAYOUT_MODERN -> createModernLayout()
                    Layouts.LAYOUT_CLASSIC -> createClassicLayout()
                    Layouts.LAYOUT_CUSTOM -> createModernLayout()  // should not happen
                }
                saveCustomLayout(mainActivity!!.baseContext, customLayout!!)
            }
        }
    }

    fun getCustomLayoutDescription(view: ViewsWithLayout): LayoutDescription {
        return when (view) {
            ViewsWithLayout.PLAYER -> customLayout!!.playerLayout
            ViewsWithLayout.ALBUM -> customLayout!!.albumLayout
            ViewsWithLayout.TITLE -> customLayout!!.titleLayout
            else -> LayoutDescription(ViewsWithLayout.PLAYER)
        }
    }

    fun setCustomLayoutDescription(view: ViewsWithLayout, layout: LayoutDescription) {
        when (view) {
            ViewsWithLayout.PLAYER -> customLayout!!.playerLayout = layout
            ViewsWithLayout.ALBUM -> customLayout!!.albumLayout = layout
            ViewsWithLayout.TITLE -> customLayout!!.titleLayout = layout
            else -> {}
        }
        saveCustomLayout(mainActivity!!.baseContext, customLayout!!)
    }
}

