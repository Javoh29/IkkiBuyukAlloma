package com.rangedroid.sayyidsafo.data.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rangedroid.sayyidsafo.data.network.response.AudioResponse

class AudioNetworkDataSourceImpl(
    private val apiService: ApiService
) : AudioNetworkDataSource {

    private val _downloadedAudios = MutableLiveData<AudioResponse>()
    override val downloadedAudios: LiveData<AudioResponse>
        get() = _downloadedAudios

    override suspend fun fetchAudios(topicId: Int, page: Int) {
        val fetchedAudios = apiService.getAudiosAsync(topicId, page).await()
        _downloadedAudios.postValue(fetchedAudios)
    }
}