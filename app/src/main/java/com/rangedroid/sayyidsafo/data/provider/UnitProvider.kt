package com.rangedroid.sayyidsafo.data.provider

interface UnitProvider {
    fun isOnline(): Boolean

    fun getSavedAudio(): String

    fun setSavedAudio(audio: String)
}