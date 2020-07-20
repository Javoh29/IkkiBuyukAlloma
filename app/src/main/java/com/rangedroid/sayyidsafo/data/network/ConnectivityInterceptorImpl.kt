package com.rangedroid.sayyidsafo.data.network

import com.rangedroid.sayyidsafo.data.provider.UnitProvider
import com.rangedroid.sayyidsafo.utils.NoConnectivityException
import okhttp3.Interceptor
import okhttp3.Response

class ConnectivityInterceptorImpl(
    private val unitProvider: UnitProvider
) : ConnectivityInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!unitProvider.isOnline())
            throw NoConnectivityException()
        return chain.proceed(chain.request())
    }
}