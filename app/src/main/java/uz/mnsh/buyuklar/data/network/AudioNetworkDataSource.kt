package uz.mnsh.buyuklar.data.network

import androidx.lifecycle.LiveData
import uz.mnsh.buyuklar.data.network.response.AudioResponse

interface AudioNetworkDataSource {

    val downloadedAudios: LiveData<AudioResponse>

    val api: ApiService

    suspend fun fetchAudios(
        topicId: Int,
        page: Int
    )
}