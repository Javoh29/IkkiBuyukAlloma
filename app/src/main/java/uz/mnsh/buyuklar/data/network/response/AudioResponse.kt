package uz.mnsh.buyuklar.data.network.response

import uz.mnsh.buyuklar.data.db.model.AudioModel
import uz.mnsh.buyuklar.data.db.model.Links
import uz.mnsh.buyuklar.data.db.model.MetaData


data class AudioResponse (
    val items: List<AudioModel>,
    val _links: Links,
    val _meta: MetaData
)