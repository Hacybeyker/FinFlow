package com.hacybeyker.finflow.feature.settings.domain.usecase

import com.hacybeyker.finflow.core.domain.PreferencesRepository
import javax.inject.Inject

class SetAppLockEnabledUseCase @Inject constructor(private val repository: PreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAppLockEnabled(enabled)
}
