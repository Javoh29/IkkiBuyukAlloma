package com.rangedroid.sayyidsafo.data.db.model

import android.content.Context
import android.os.Parcelable
import android.support.v4.media.MediaDescriptionCompat
import androidx.annotation.Keep
import androidx.core.graphics.drawable.toBitmap
import androidx.room.ColumnInfo
import com.rangedroid.sayyidsafo.R
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UnitAudiosModel(
    @ColumnInfo(name = "idTable")
    val idTable: Int,
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "location")
    val location: String,
    @ColumnInfo(name = "topic_id")
    val topic_id: String,
    @ColumnInfo(name = "size")
    val size: Long,
    @ColumnInfo(name = "duration")
    val duration: Long
): Parcelable{

    fun getDescription(context: Context): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
            .setTitle(name)
            .setIconBitmap(context.getDrawable(R.drawable.splashlogo)?.toBitmap())
            .build()
    }

    fun getFileName(): String {
        return "$name.mp3"
    }
}