package uz.mnsh.buyuklar.data.network

import okhttp3.Interceptor
import okhttp3.Response
import uz.mnsh.buyuklar.data.provider.UnitProvider
import uz.mnsh.buyuklar.utils.NoConnectivityException

class ConnectivityInterceptorImpl(
    private val unitProvider: UnitProvider
) : ConnectivityInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!unitProvider.isOnline())
            throw NoConnectivityException()
        return chain.proceed(chain.request())
    }
}