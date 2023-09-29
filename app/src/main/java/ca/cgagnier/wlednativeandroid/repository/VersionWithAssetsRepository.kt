package ca.cgagnier.wlednativeandroid.repository

import androidx.annotation.WorkerThread
import androidx.room.Transaction
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import kotlinx.coroutines.flow.Flow

class VersionWithAssetsRepository(deviceDatabase: DevicesDatabase) {
    private val versionDao = deviceDatabase.versionDao()
    private val assetDao = deviceDatabase.assetDao()

    val allVersionsWithAssets: Flow<List<VersionWithAssets>> = versionDao.getVersionsWithAsset()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    @Transaction
    suspend fun insertMany(versions: List<Version>, assets: List<Asset>) {
        versionDao.insertMany(versions)
        assetDao.insertMany(assets)
    }

    suspend fun getLatestVersionWithAssets(): VersionWithAssets? {
        return versionDao.getLatestVersionWithAssets()
    }
}