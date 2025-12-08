package com.miraimagiclab.novelreadingapp.ui.onboarding

import androidx.lifecycle.ViewModel
import com.miraimagiclab.novelreadingapp.data.auth.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
) : ViewModel() {
    fun onGetStarted() {
        tokenStorage.setOnboardingComplete()
    }
}