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


    init {
        GlobalScope.launch(Dispatchers.Default) {
            if (unitProvider.isOnline() && !isDownload){
                fetchAudios()
            }
        }
        audiosNetworkDataSource.apply {
            downloadedAudios.observeForever {
                if (it == null) return@observeForever
                persistFetchedAudios(it)
            }
        }
    }

    override suspend fun getAudios(): LiveData<List<UnitAudiosModel>> {
        return withContext(Dispatchers.IO){
            return@withContext audiosDao.getAudios()
        }
    }

    private fun persistFetchedAudios(audiosResponse: AudioResponse){
        GlobalScope.launch(Dispatchers.IO) {
            audiosDao.deleteAudios()
            audiosResponse.items.forEach {
                audiosDao.upsertAudios(it)
            }
            isDownload = true
        }
    }

    private suspend fun fetchAudios(){
        audiosNetworkDataSource.fetchAudios(1, 1)
    }
}