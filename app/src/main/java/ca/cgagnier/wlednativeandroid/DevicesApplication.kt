package ca.cgagnier.wlednativeandroid

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DevicesDatabase
import ca.cgagnier.wlednativeandroid.repository.UserPreferences
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesSerializer
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.repository.migrations.UserPreferencesV0ToV1
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.service.device.StateFactory

private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

class DevicesApplication : Application() {
    private val database by lazy { DevicesDatabase.getDatabase(this) }
    val deviceRepository by lazy { DeviceRepository(database) }
    val deviceDiscovery by lazy { DeviceDiscovery(this) }
    val versionWithAssetsRepository by lazy { VersionWithAssetsRepository(database) }
    val deviceStateFactory by lazy { StateFactory(this) }

    private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer(),
        produceMigrations = { _ ->
            listOf(UserPreferencesV0ToV1())
        }
    )

    val userPreferencesRepository by lazy { UserPreferencesRepository(userPreferencesStore) }
}