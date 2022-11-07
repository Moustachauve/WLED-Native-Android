package ca.cgagnier.wlednativeandroid.repository

import android.util.Log
import androidx.datastore.core.DataStore
import ca.cgagnier.wlednativeandroid.model.Device
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

class UserPreferencesRepository(private val dataStore: DataStore<UserPreferences>) {
    private val TAG: String = "UserPreferencesRepo"

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun fetchInitialPreferences() = userPreferencesFlow.first()

    suspend fun updateSelectedDevice(device: Device) {
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setSelectedDeviceAddress(device.address)
                .build()
        }
    }

    suspend fun updatehasMigratedSharedPref(hasMigrated: Boolean) {
        dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setHasMigratedSharedPref(hasMigrated)
                .build()
        }
    }
}