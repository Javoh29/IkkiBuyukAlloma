package uz.mnsh.buyuklar.data.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import uz.mnsh.buyuklar.data.network.response.AudioResponse

class AudioNetworkDataSourceImpl(
    private val apiService: ApiService
) : AudioNetworkDataSource {

    private val _downloadedAudios = MutableLiveData<AudioResponse>()
    override val downloadedAudios: LiveData<AudioResponse>
        get() = _downloadedAudios
    override val api: ApiService
        get() = apiService

    override suspend fun fetchAudios(topicId: Int, page: Int) {
        val fetchedAudios = apiService.getAudiosAsync(topicId, page).await()
        _downloadedAudios.postValue(fetchedAudios)
    }
}