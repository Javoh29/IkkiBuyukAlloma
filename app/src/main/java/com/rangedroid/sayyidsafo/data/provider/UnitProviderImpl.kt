package com.rangedroid.sayyidsafo.data.provider

import android.content.Context
import android.net.ConnectivityManager
import java.io.IOException
import java.net.InetAddress

class UnitProviderImpl(private val context: Context) : UnitProvider {
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
}