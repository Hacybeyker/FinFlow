package com.hacybeyker.finflow.feature.settings.domain.usecase

import com.hacybeyker.finflow.feature.settings.domain.PreferencesRepository
import com.hacybeyker.finflow.feature.settings.domain.ThemeMode
import javax.inject.Inject

class SetThemeModeUseCase @Inject constructor(private val repository: PreferencesRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}
