package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Profile
import com.medstock.app.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: AppRepository) : ViewModel() {

    val profile: StateFlow<Profile?> = repo.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(profile: Profile, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repo.saveProfile(profile)
            onDone()
        }
    }
}
