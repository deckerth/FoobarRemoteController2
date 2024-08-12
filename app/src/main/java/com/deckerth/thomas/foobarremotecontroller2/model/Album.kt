package com.deckerth.thomas.foobarremotecontroller2.model

data class Album(
    val originalTitle: ITitle
){
    val titles = mutableListOf<ITitle>()

    fun hasIndex(index:String): Boolean{
        return titles.find { it.index == index } != null
    }
}
