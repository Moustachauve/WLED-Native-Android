package ca.cgagnier.wlednativeandroid

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import ca.cgagnier.wlednativeandroid.repository.AssetDao
import ca.cgagnier.wlednativeandroid.repository.DeviceDao
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.DevicesDatabase
import ca.cgagnier.wlednativeandroid.repository.UserPreferences
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesRepository
import ca.cgagnier.wlednativeandroid.repository.UserPreferencesSerializer
import ca.cgagnier.wlednativeandroid.repository.VersionDao
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.repository.migrations.UserPreferencesV0ToV1
import ca.cgagnier.wlednativeandroid.service.device.StateFactory
import ca.cgagnier.wlednativeandroid.service.device.api.JsonApiRequestHandler
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = UserPreferencesSerializer(),
    produceMigrations = { _ ->
        listOf(UserPreferencesV0ToV1())
    }
)

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
    fun provideVersionWithAssetsRepository(
        versionDao: VersionDao, assetDao: AssetDao
    ): VersionWithAssetsRepository {
        return VersionWithAssetsRepository(versionDao, assetDao)
    }

    @Provides
    @Singleton
    fun provideStateFactory(
        deviceRepository: DeviceRepository, versionWithAssetsRepository: VersionWithAssetsRepository
    ): StateFactory {
        val releaseService = ReleaseService(versionWithAssetsRepository)
        return StateFactory(JsonApiRequestHandler(deviceRepository, releaseService))
    }

    @Provides
    @Singleton
    fun provideUserPreferencesStore(
        @ApplicationContext appContext: Context
    ): DataStore<UserPreferences> {
        return appContext.userPreferencesStore
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext appContext: Context
    ): UserPreferencesRepository {
        return UserPreferencesRepository(appContext.userPreferencesStore)
    }
}