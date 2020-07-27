package com.rangedroid.sayyidsafo.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.rangedroid.sayyidsafo.data.repository.AudiosRepository
import com.rangedroid.sayyidsafo.utils.lazyDeferred

class InfoViewModel(
    private val audiosRepository: AudiosRepository
) : ViewModel() {

    private val _index = MutableLiveData<Int>()
    val text: LiveData<Int> = Transformations.map(_index) {
        it
    }

    fun setIndex(index: Int) {
        _index.value = index
    }

    fun getFirst() = lazyDeferred{
        audiosRepository.getFirst(733)
    }

}