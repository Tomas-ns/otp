package pt.isel

import android.app.Application
import android.content.Intent
import pt.isel.repository.SettingsPreferenceRepository
import pt.isel.services.TransportDetectionService
import pt.isel.settings.domain.repository.SettingsRepository

class OTPCDApplication: Application() {
    lateinit var settingsRepository: SettingsRepository
    private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsPreferenceRepository(dataStore)

        startForegroundService(Intent(this, TransportDetectionService::class.java))
    }
}