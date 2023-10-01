package ca.cgagnier.wlednativeandroid.repository

import android.util.Log
import androidx.datastore.core.DataStore
import ca.cgagnier.wlednativeandroid.model.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<UserPreferences>) {

    private val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }

    val themeMode get() = dataStore.data.map { it.theme }
    val selectedDeviceAddress get() = dataStore.data.map { it.selectedDeviceAddress }
    val autoDiscovery get() = dataStore.data.map { it.automaticDiscovery }
    val showOfflineDevicesLast get() = dataStore.data.map { it.showOfflineLast }
    val sendCrashData get() = dataStore.data.map { it.sendCrashData }
    val sendPerformanceData get() = dataStore.data.map { it.sendPerformanceData }
    val lastUpdateCheckDate get() = dataStore.data.map { it.lastUpdateCheckDate }

    suspend fun fetchInitialPreferences() = userPreferencesFlow.first()

    suspend fun updateSelectedDevice(device: Device) {
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setSelectedDeviceAddress(device.address)
                .build()
        }
    }

    suspend fun updateHasMigratedSharedPref(hasMigrated: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setHasMigratedSharedPref(hasMigrated)
                .build()
        }
    }

    suspend fun updateThemeMode(themeSettings: ThemeSettings) {
        dataStore.updateData {
            it.toBuilder().setTheme(themeSettings).build()
        }
    }

    suspend fun updateAutoDiscovery(autoDiscover: Boolean) {
        dataStore.updateData {
            it.toBuilder().setAutomaticDiscovery(autoDiscover).build()
        }
    }

    suspend fun updateShowOfflineDeviceLast(showOfflineDeviceLast: Boolean) {
        dataStore.updateData {
            it.toBuilder().setShowOfflineLast(showOfflineDeviceLast).build()
        }
    }

    suspend fun updateSendCrashData(sendCrashData: Boolean) {
        dataStore.updateData {
            it.toBuilder().setSendCrashData(sendCrashData).build()
        }
    }

    suspend fun updateSendPerformanceData(sendPerformanceData: Boolean) {
        dataStore.updateData {
            it.toBuilder().setSendPerformanceData(sendPerformanceData).build()
        }
    }

    suspend fun updateLastUpdateCheckDate(lastUpdateCheckDate: Long) {
        dataStore.updateData {
            it.toBuilder().setLastUpdateCheckDate(lastUpdateCheckDate).build()
        }
    }

    companion object {
        private const val TAG: String = "UserPreferencesRepo"
    }
}