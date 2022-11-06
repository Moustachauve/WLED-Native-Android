package ca.cgagnier.wlednativeandroid

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DevicesDatabase
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesSerializer
import com.codelab.android.datastore.UserPreferences

private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

class DevicesApplication : Application() {

    val database by lazy { DevicesDatabase.getDatabase(this) }
    val repository by lazy { DeviceRepository(database) }

    val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer()
    )
    val userPreferencesRepository by lazy { UserPreferencesRepository(userPreferencesStore)  }
}