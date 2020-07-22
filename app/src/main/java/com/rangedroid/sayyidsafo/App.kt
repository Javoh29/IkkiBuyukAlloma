package com.rangedroid.sayyidsafo

import android.app.Application
import android.content.ServiceConnection
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.rangedroid.sayyidsafo.data.db.AudiosDatabase
import com.rangedroid.sayyidsafo.data.network.*
import com.rangedroid.sayyidsafo.data.provider.UnitProvider
import com.rangedroid.sayyidsafo.data.provider.UnitProviderImpl
import com.rangedroid.sayyidsafo.data.repository.AudiosRepository
import com.rangedroid.sayyidsafo.data.repository.AudiosRepositoryImpl
import com.rangedroid.sayyidsafo.ui.fragment.PageViewModelFactory
import com.rangedroid.sayyidsafo.utils.AudioPlayerService
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class App: Application(), KodeinAware {

    override val kodein: Kodein
        get() = Kodein.lazy {
            import(androidXModule(this@App))

            bind() from singleton { AudiosDatabase(instance()) }
            bind() from singleton { instance<AudiosDatabase>().audiosDao() }
            bind<UnitProvider>() with singleton { UnitProviderImpl(instance()) }
            bind<ConnectivityInterceptor>() with singleton { ConnectivityInterceptorImpl(instance()) }
            bind() from singleton { ApiService(instance()) }
            bind<AudioNetworkDataSource>() with singleton { AudioNetworkDataSourceImpl(instance()) }
            bind<AudiosRepository>() with singleton { AudiosRepositoryImpl(instance(), instance(), instance()) }
            bind() from provider { PageViewModelFactory(instance()) }
        }

    companion object {
        var isDownload: Boolean = false
        const val BASE_URL = "http://5.182.26.44/"
        var DIR_PATH = ""
        var binder: AudioPlayerService.LocalBinder? = null
        var connection: ServiceConnection? = null
    }

    override fun onCreate() {
        super.onCreate()
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        PRDownloader.initialize(applicationContext, config)
    }

}