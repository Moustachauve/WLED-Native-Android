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
        Log.d(TAG, "updateSelectedDevice")
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setSelectedDeviceAddress(device.address)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun updateHasMigratedSharedPref(hasMigrated: Boolean) {
        Log.d(TAG, "updateHasMigratedSharedPref")
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setHasMigratedSharedPref(hasMigrated)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun updateThemeMode(themeSettings: ThemeSettings) {
        Log.d(TAG, "updateThemeMode")
        dataStore.updateData {
            it.toBuilder()
                .setTheme(themeSettings)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun updateAutoDiscovery(autoDiscover: Boolean) {
        Log.d(TAG, "updateAutoDiscovery")
        dataStore.updateData {
            it.toBuilder()
                .setAutomaticDiscovery(autoDiscover)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun updateShowOfflineDeviceLast(showOfflineDeviceLast: Boolean) {
        Log.d(TAG, "updateShowOfflineDeviceLast")
        dataStore.updateData {
            it.toBuilder()
                .setShowOfflineLast(showOfflineDeviceLast)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun updateSendCrashData(sendCrashData: Boolean) {
        Log.d(TAG, "updateSendCrashData")
        dataStore.updateData {
            it.toBuilder()
                .setSendCrashData(sendCrashData)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun updateSendPerformanceData(sendPerformanceData: Boolean) {
        Log.d(TAG, "updateSendPerformanceData")
        dataStore.updateData {
            it.toBuilder()
                .setSendPerformanceData(sendPerformanceData)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun updateLastUpdateCheckDate(lastUpdateCheckDate: Long) {
        Log.d(TAG, "updateLastUpdateCheckDate")
        dataStore.updateData {
            it.toBuilder()
                .setLastUpdateCheckDate(lastUpdateCheckDate)
                .setDateLastWritten(System.currentTimeMillis())
                .build()
        }
    }

    companion object {
        private const val TAG: String = "UserPreferencesRepo"
    }
}