package com.rangedroid.sayyidsafo.data.repository

import androidx.lifecycle.LiveData
import com.rangedroid.sayyidsafo.data.db.model.AudioModel

interface AudiosRepository {
    suspend fun getAudios(): LiveData<List<AudioModel>>
    suspend fun getFirst(index: Int): LiveData<AudioModel>
}