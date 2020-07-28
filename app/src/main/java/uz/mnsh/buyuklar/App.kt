package uz.mnsh.buyuklar

import android.app.Application
import android.content.ServiceConnection
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import uz.mnsh.buyuklar.data.db.AudiosDatabase
import uz.mnsh.buyuklar.data.network.*
import uz.mnsh.buyuklar.data.provider.UnitProvider
import uz.mnsh.buyuklar.data.provider.UnitProviderImpl
import uz.mnsh.buyuklar.data.repository.AudiosRepository
import uz.mnsh.buyuklar.data.repository.AudiosRepositoryImpl
import uz.mnsh.buyuklar.ui.fragment.InfoViewModelFactory
import uz.mnsh.buyuklar.ui.fragment.PageViewModelFactory
import uz.mnsh.buyuklar.utils.AudioPlayerService

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
            bind() from provider { InfoViewModelFactory(instance()) }
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