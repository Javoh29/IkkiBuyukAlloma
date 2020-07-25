package com.rangedroid.sayyidsafo.data.repository

import androidx.lifecycle.LiveData
import com.rangedroid.sayyidsafo.App.Companion.isDownload
import com.rangedroid.sayyidsafo.data.db.AudiosDao
import com.rangedroid.sayyidsafo.data.db.model.UnitAudiosModel
import com.rangedroid.sayyidsafo.data.network.AudioNetworkDataSource
import com.rangedroid.sayyidsafo.data.network.response.AudioResponse
import com.rangedroid.sayyidsafo.data.provider.UnitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudiosRepositoryImpl(
    private val audiosDao: AudiosDao,
    private val audiosNetworkDataSource: AudioNetworkDataSource,
    private val unitProvider: UnitProvider
) : AudiosRepository {

    private var isLoaded: Boolean = false

    init {
        fetchAudios(1)
        audiosNetworkDataSource.apply {
            downloadedAudios.observeForever {
                if (it == null) return@observeForever
                if (isLoaded){
                    isLoaded = false
                    fetchAudios(it._meta.pageCount)
                }else{
                    persistFetchedAudios(it)
                }
            }
        }
    }

    override suspend fun getAudios(): LiveData<List<UnitAudiosModel>> {
        return withContext(Dispatchers.IO){
            return@withContext audiosDao.getAudios()
        }
    }

    override suspend fun getFirst(index: Int): LiveData<UnitAudiosModel> {
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

    private fun fetchAudios(size: Int){
        GlobalScope.launch(Dispatchers.Default){
            if (unitProvider.isOnline() && !isDownload){
                audiosDao.deleteAudios()
                if (size > 1){
                    isDownload = true
                }else{
                    isLoaded = true
                }
                for (i: Int in 1..size){
                    audiosNetworkDataSource.fetchAudios(8, i)
                }
            }
        }
    }
}