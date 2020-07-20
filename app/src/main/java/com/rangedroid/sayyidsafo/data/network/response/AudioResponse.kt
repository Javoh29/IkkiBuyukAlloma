package com.rangedroid.sayyidsafo.data.network.response

import com.rangedroid.sayyidsafo.data.db.model.AudioModel
import com.rangedroid.sayyidsafo.data.db.model.Links
import com.rangedroid.sayyidsafo.data.db.model.MetaData

data class AudioResponse (
    val items: List<AudioModel>,
    val _links: Links,
    val _meta: MetaData
)