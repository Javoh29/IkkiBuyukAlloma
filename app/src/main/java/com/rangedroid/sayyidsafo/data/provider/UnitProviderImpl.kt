package com.rangedroid.sayyidsafo.data.provider

import android.content.Context
import android.net.ConnectivityManager
import java.io.IOException
import java.net.InetAddress

class UnitProviderImpl(private val context: Context) : PreferenceProvider(context), UnitProvider {

    private val audioSaved = "AUDIO_SAVED"

    override fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) {
            try {
                return !InetAddress.getByName("google.com").equals("")
            } catch (e: IOException) {
                false
            }
        }else false
    }

    override fun getSavedAudio(): String {
        return preferences.getString(audioSaved, "not")!!
    }

    override fun setSavedAudio(audio: String) {
        preferences.edit().putString(audioSaved, audio).apply()
    }
}