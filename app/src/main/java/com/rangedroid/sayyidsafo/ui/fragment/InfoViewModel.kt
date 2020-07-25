package com.rangedroid.sayyidsafo.ui.fragment

import androidx.lifecycle.ViewModel
import com.rangedroid.sayyidsafo.data.repository.AudiosRepository
import com.rangedroid.sayyidsafo.utils.lazyDeferred

class InfoViewModel(
    private val audiosRepository: AudiosRepository
) : ViewModel() {

    fun getFirst() = lazyDeferred{
        audiosRepository.getFirst(733)
    }

}