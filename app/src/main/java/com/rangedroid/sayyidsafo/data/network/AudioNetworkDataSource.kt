package com.rangedroid.sayyidsafo.data.network

import androidx.lifecycle.LiveData
import com.rangedroid.sayyidsafo.data.network.response.AudioResponse

interface AudioNetworkDataSource {

    val downloadedAudios: LiveData<AudioResponse>

    suspend fun fetchAudios(
        topicId: Int,
        page: Int
    )
}