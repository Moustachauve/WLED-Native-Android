package ca.cgagnier.wlednativeandroid

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import ca.cgagnier.wlednativeandroid.repository.*
import ca.cgagnier.wlednativeandroid.repository.migrations.UserPreferencesV0ToV1

private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

class DevicesApplication : Application() {

    val database by lazy { DevicesDatabase.getDatabase(this) }
    val repository by lazy { DeviceRepository(database) }

    val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer(),
        produceMigrations = { _ ->
            listOf(UserPreferencesV0ToV1())
        }
    )

    val userPreferencesRepository by lazy { UserPreferencesRepository(userPreferencesStore)  }
}