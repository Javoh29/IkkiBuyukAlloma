package uz.mnsh.buyuklar.data.repository

import androidx.lifecycle.LiveData
import uz.mnsh.buyuklar.App.Companion.isDownload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.mnsh.buyuklar.data.db.AudiosDao
import uz.mnsh.buyuklar.data.db.model.AudioModel
import uz.mnsh.buyuklar.data.network.AudioNetworkDataSource
import uz.mnsh.buyuklar.data.network.response.AudioResponse
import uz.mnsh.buyuklar.data.provider.UnitProvider

class AudiosRepositoryImpl(
    private val audiosDao: AudiosDao,
    private val audiosNetworkDataSource: AudioNetworkDataSource,
    private val unitProvider: UnitProvider
) : AudiosRepository {

    init {
        fetchAudios()
        audiosNetworkDataSource.apply {
            downloadedAudios.observeForever {
                if (it == null) return@observeForever
                persistFetchedAudios(it)
            }
        }
    }

    override suspend fun getAudios(id: Int): LiveData<List<AudioModel>> {
        return withContext(Dispatchers.IO){
            return@withContext audiosDao.getAudios(id)
        }
    }

    override suspend fun getFirst(index: Int): LiveData<AudioModel> {
        return withContext(Dispatchers.IO){
            return@withContext audiosDao.getFirst(index)
        }
    }

    private fun persistFetchedAudios(audiosResponse: AudioResponse){
        GlobalScope.launch(Dispatchers.IO) {
            audiosResponse.items.forEach {
                audiosDao.upsertAudios(it)
            }
        }
    }

    private fun fetchAudios(){
        GlobalScope.launch(Dispatchers.Default){
            if (unitProvider.isOnline() && !isDownload){
                audiosDao.deleteAudios()
                val fetchResponseOne = audiosNetworkDataSource.api.getAudiosAsync(8,1).await()
                persistFetchedAudios(fetchResponseOne)
                for (i in 2..fetchResponseOne._meta.pageCount){
                    audiosNetworkDataSource.fetchAudios(8, i)
                }
                val fetchResponseTwo = audiosNetworkDataSource.api.getAudiosAsync(9,1).await()
                persistFetchedAudios(fetchResponseTwo)
                for (i in 2..fetchResponseTwo._meta.pageCount){
                    audiosNetworkDataSource.fetchAudios(9, i)
                }
                isDownload = true
            }
        }
    }
}