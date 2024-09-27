package ca.cgagnier.wlednativeandroid

import android.content.Context
import ca.cgagnier.wlednativeandroid.repository.AssetDao
import ca.cgagnier.wlednativeandroid.repository.DeviceDao
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DevicesDatabase
import ca.cgagnier.wlednativeandroid.repository.VersionDao
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

@Module
@InstallIn(SingletonComponent::class)
object AppContainer {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): DevicesDatabase {
        return DevicesDatabase.getDatabase(appContext)
    }

    @Provides
    @Singleton
    fun provideDeviceDao(appDatabase: DevicesDatabase): DeviceDao {
        return appDatabase.deviceDao()
    }
    @Provides
    @Singleton
    fun provideVersionDao(appDatabase: DevicesDatabase): VersionDao {
        return appDatabase.versionDao()
    }
    @Provides
    @Singleton
    fun provideAssetDao(appDatabase: DevicesDatabase): AssetDao {
        return appDatabase.assetDao()
    }
    @Provides
    @Singleton
    fun provideDeviceRepository(deviceDao: DeviceDao): DeviceRepository {
        return DeviceRepository(deviceDao)
    }

    @Provides
    @Singleton
    fun provideVersionWithAssetsRepository(versionDao: VersionDao, assetDao: AssetDao): VersionWithAssetsRepository {
        return VersionWithAssetsRepository(versionDao, assetDao)
    }
    @Provides
    @Singleton
    fun provideStateFactory(): StateFactory {
        return StateFactory()
    }

    //val deviceDiscovery by lazy { DeviceDiscovery(context) }
    //val deviceStateFactory by lazy { StateFactory(this) }

    /*private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer(),
        produceMigrations = { _ ->
            listOf(UserPreferencesV0ToV1())
        }
    )*/

    //val userPreferencesRepository by lazy { UserPreferencesRepository(context.userPreferencesStore) }
}