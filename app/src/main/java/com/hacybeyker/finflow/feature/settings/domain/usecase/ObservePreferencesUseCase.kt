package com.hacybeyker.finflow.feature.settings.domain.usecase

import com.hacybeyker.finflow.feature.settings.domain.PreferencesRepository
import com.hacybeyker.finflow.feature.settings.domain.UserPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** Reactive stream of the user's settings; emits again on every change. */
class ObservePreferencesUseCase @Inject constructor(private val repository: PreferencesRepository) {
    operator fun invoke(): Flow<UserPreferences> = repository.observePreferences()
}
