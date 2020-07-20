package com.rangedroid.sayyidsafo.data.repository

import androidx.lifecycle.LiveData
import com.rangedroid.sayyidsafo.data.db.model.UnitAudiosModel

interface AudiosRepository {
    suspend fun getAudios(): LiveData<List<UnitAudiosModel>>
}