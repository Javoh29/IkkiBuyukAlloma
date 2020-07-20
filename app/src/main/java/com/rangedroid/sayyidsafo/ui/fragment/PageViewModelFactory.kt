package com.rangedroid.sayyidsafo.ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rangedroid.sayyidsafo.data.repository.AudiosRepository

class PageViewModelFactory(
    private val audiosRepository: AudiosRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PageViewModel(audiosRepository) as T
    }
}