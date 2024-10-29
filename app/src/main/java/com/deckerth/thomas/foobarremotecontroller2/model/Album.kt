package com.deckerth.thomas.foobarremotecontroller2.model

data class Album(
    val originalTitle: ITitle
){
    private var _titles = mutableListOf<ITitle>()

    val titles: List<ITitle>
        get() { return _titles.toList() }

    var isSelected = false

    var isAutomaticSelection = true

    var endIndex: Int = 0

    fun hasIndex(index:Int): Boolean{
        return index in originalTitle.index..endIndex
    }

    fun addTitle(title: ITitle) {
        _titles.add(title)
        endIndex=title.index
    }
}

fun MutableList<ITitle>.addTitle(title: ITitle,album: Album) {
    this.add(title)
    album.endIndex = title.index
}
