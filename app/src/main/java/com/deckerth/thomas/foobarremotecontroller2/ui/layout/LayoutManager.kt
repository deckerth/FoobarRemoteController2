package com.deckerth.thomas.foobarremotecontroller2.ui.layout

import androidx.compose.runtime.Composable
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
        fields.items.add(LayoutItem(LayoutItems.ARTWORK))
        fields.items.add(LayoutItem(LayoutItems.TITLE, Typography.TITLE_LARGE))
        fields.items.add(LayoutItem(LayoutItems.ALBUM, Typography.BODY_SMALL))
        fields.items.add(LayoutItem(LayoutItems.PROGRESS))
        return fields
    }

    private fun createModernAlbumLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.ALBUM, Typography.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, Typography.BODY_SMALL))
        fields.items.add(LayoutItem(LayoutItems.COMPOSER, Typography.BODY_SMALL))
        return fields
    }

    private fun createModernTitleLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.TITLE, Typography.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, Typography.BODY_SMALL))
        return fields
    }

    private fun createClassicLayout(): Layout {
        return Layout(playerLayout = createClassicPlayerLayout(),
                      albumLayout = createClassicAlbumLayout(),
                      titleLayout = createClassicTitleLayout())
    }

    private fun createClassicPlayerLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.ARTWORK))
        fields.items.add(LayoutItem(LayoutItems.COMPOSER, Typography.TITLE_LARGE))
        fields.items.add(LayoutItem(LayoutItems.ALBUM, Typography.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, Typography.BODY_SMALL))
        fields.items.add(LayoutItem(LayoutItems.PROGRESS))
        return fields
    }

    private fun createClassicAlbumLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.COMPOSER, Typography.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ALBUM, Typography.BODY_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, Typography.BODY_SMALL))
        return fields
    }

    private fun createClassicTitleLayout(): LayoutDescription {
        val fields = LayoutDescription()
        fields.items.add(LayoutItem(LayoutItems.TITLE, Typography.TITLE_MEDIUM))
        fields.items.add(LayoutItem(LayoutItems.ARTIST, Typography.BODY_SMALL))
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