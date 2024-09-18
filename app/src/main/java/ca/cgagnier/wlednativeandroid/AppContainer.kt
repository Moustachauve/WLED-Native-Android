package ca.cgagnier.wlednativeandroid

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

/**
 * [AppContainer] implementation that provides instance of [DeviceRepository]
 */
class AppContainer(private val context: Context) {
    private val database by lazy { DevicesDatabase.getDatabase(context) }
    val deviceRepository by lazy { DeviceRepository(database) }
    val deviceDiscovery by lazy { DeviceDiscovery(context) }
    val versionWithAssetsRepository by lazy { VersionWithAssetsRepository(database) }
    val deviceStateFactory by lazy { StateFactory(this) }

    private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer(),
        produceMigrations = { _ ->
            listOf(UserPreferencesV0ToV1())
        }
    )

    val userPreferencesRepository by lazy { UserPreferencesRepository(context.userPreferencesStore) }
}