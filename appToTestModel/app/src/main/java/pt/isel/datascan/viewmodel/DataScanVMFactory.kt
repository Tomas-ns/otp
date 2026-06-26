package pt.isel.datascan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.isel.settings.domain.repository.SettingsRepository

@Suppress("UNCHECKED_CAST")
class DataScanVMFactory(
    private val repository: SettingsRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DataScanViewModel(repository) as T
    }
}