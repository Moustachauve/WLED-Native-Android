package ca.cgagnier.wlednativeandroid

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import ca.cgagnier.wlednativeandroid.repository.*
import ca.cgagnier.wlednativeandroid.repository.migrations.UserPreferencesV0ToV1
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery

private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

class DevicesApplication : Application() {
    private val database by lazy { DevicesDatabase.getDatabase(this) }
    val deviceRepository by lazy { DeviceRepository(database) }
    val deviceDiscovery by lazy { DeviceDiscovery(this) }
    val versionWithAssetsRepository by lazy { VersionWithAssetsRepository(database) }

    private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer(),
        produceMigrations = { _ ->
            listOf(UserPreferencesV0ToV1())
        }
    )

    val userPreferencesRepository by lazy { UserPreferencesRepository(userPreferencesStore) }
}