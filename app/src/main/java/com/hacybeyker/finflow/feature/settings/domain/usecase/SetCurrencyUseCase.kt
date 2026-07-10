package com.hacybeyker.finflow.feature.settings.domain.usecase

import com.hacybeyker.finflow.core.domain.PreferencesRepository
import javax.inject.Inject

/** Persists the display currency; `null` returns to the device-locale currency. */
class SetCurrencyUseCase @Inject constructor(private val repository: PreferencesRepository) {
    suspend operator fun invoke(code: String?) = repository.setCurrencyCode(code)
}
