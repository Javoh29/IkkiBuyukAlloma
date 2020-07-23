package com.rangedroid.sayyidsafo.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rangedroid.sayyidsafo.data.db.model.AudioModel
import com.rangedroid.sayyidsafo.data.db.model.UnitAudiosModel

@Dao
interface AudiosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAudios(audioModel: AudioModel)

    @Query("SELECT * from audios_table")
    fun getAudios(): LiveData<List<UnitAudiosModel>>

    @Query("DELETE FROM audios_table")
    fun deleteAudios()

    @Query("select * from audios_table where id == 733")
    fun getFirst(): LiveData<UnitAudiosModel>
}