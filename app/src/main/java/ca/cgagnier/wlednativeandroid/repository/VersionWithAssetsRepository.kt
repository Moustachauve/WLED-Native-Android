package ca.cgagnier.wlednativeandroid.repository

import androidx.annotation.WorkerThread
import androidx.room.Transaction
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import javax.inject.Inject

class VersionWithAssetsRepository @Inject constructor(private val versionDao: VersionDao, private val assetDao: AssetDao) {

    @WorkerThread
    @Transaction
    suspend fun insertMany(versions: List<Version>, assets: List<Asset>) {
        versionDao.insertMany(versions)
        assetDao.insertMany(assets)
    }

    suspend fun getLatestStableVersionWithAssets(): VersionWithAssets? {
        return versionDao.getLatestStableVersionWithAssets()
    }
    suspend fun getLatestBetaVersionWithAssets(): VersionWithAssets? {
        return versionDao.getLatestBetaVersionWithAssets()
    }

    suspend fun getVersionByTag(tagName: String): VersionWithAssets? {
        return versionDao.getVersionByTagName(tagName)
    }

    fun removeAll() {
        versionDao.removeAll()
    }
}