package com.rangedroid.sayyidsafo.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rangedroid.sayyidsafo.data.db.model.AudioModel

@Dao
interface AudiosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAudios(audioModel: AudioModel)

    @Query("SELECT * from audios_table")
    fun getAudios(): LiveData<List<AudioModel>>

    @Query("DELETE FROM audios_table")
    fun deleteAudios()

    @Query("select * from audios_table where id == :index")
    fun getFirst(index: Int): LiveData<AudioModel>
}