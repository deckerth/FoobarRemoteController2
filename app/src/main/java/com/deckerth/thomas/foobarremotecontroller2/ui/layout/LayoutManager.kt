package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import com.deckerth.thomas.foobarremotecontroller2.getViewMode

class LayoutManager {
    
    private val classicLayout : Layout = createClassicLayout()
    private val modernLayout : Layout = createModernLayout()

    private fun createModernLayout(): Layout {
        return Layout(playerLayout = createModernPlayerLayout(),
            albumLayout = createModernAlbumLayout(),
            titleLayout = createModernTitleLayout())
    }

    private fun createModernPlayerLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.ARTWORK, ItemSize.LARGE_COVER))
        fields.items.add(LayoutItem(LayoutItems.TITLE, ItemSize.TITLE_LARGE))
        fields.items.add(LayoutItem(LayoutItems.ALBUM, ItemSize.BODY_SMALL))
        fields.items.add(LayoutItem(LayoutItems.PROGRESS))
        return fields
    }

    private fun createModernAlbumLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.ALBUM, ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, ItemSize.BODY_SMALL))
        fields.items.add(LayoutItem(LayoutItems.COMPOSER, ItemSize.BODY_SMALL))
        return fields
    }

    private fun createModernTitleLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.TITLE, ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.SMART_ARTIST, ItemSize.BODY_SMALL))
        return fields
    }

    private fun createClassicLayout(): Layout {
        return Layout(playerLayout = createClassicPlayerLayout(),
                      albumLayout = createClassicAlbumLayout(),
                      titleLayout = createClassicTitleLayout())
    }

    private fun createClassicPlayerLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.ARTWORK, ItemSize.MEDIUM_COVER))
        fields.items.add(LayoutItem(LayoutItems.COMPOSER, ItemSize.TITLE_LARGE))
        fields.items.add(LayoutItem(LayoutItems.ALBUM, ItemSize.TITLE_MEDIUM, fontStyle = FontStyle.Italic))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, ItemSize.BODY_SMALL, maxLines = 10))
        fields.items.add(LayoutItem(LayoutItems.PROGRESS))
        return fields
    }

    private fun createClassicAlbumLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.COMPOSER, ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ALBUM, ItemSize.BODY_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, ItemSize.BODY_SMALL))
        return fields
    }

    private fun createClassicTitleLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.TITLE, ItemSize.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.SMART_ARTIST, ItemSize.BODY_SMALL))
        return fields
    }

    @Composable
    fun getLayout() : Layout {
        return when(getViewMode()) {
            "Modern" -> modernLayout
            "Classic" -> classicLayout
            else -> modernLayout
        }
    }

}