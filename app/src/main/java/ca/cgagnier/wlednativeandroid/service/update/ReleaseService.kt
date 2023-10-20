package ca.cgagnier.wlednativeandroid.service.update

import android.content.Context
import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.model.githubapi.Release
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.api.github.GithubApi
import com.vdurmont.semver4j.Semver


class ReleaseService(private val versionWithAssetsRepository: VersionWithAssetsRepository) {

    suspend fun getUpdateVersionTagAvailable(versionName: String, ignoreVersion: String): String {
        if (versionName == Device.UNKNOWN_VALUE) {
            return ""
        }
        val latestVersion = versionWithAssetsRepository.getLatestVersionWithAssets() ?: return ""
        if (latestVersion.version.tagName == ignoreVersion) {
            return ""
        }
        return if (Semver(latestVersion.version.tagName.drop(1)).isGreaterThan(versionName)) {
            latestVersion.version.tagName
        } else {
            ""
        }
    }

    suspend fun refreshVersions(context: Context) {
        val allVersions = GithubApi(context).getAllReleases()

        if (allVersions == null) {
            Log.w(TAG, "Did not find any version")
            return
        }

        val versionModels = mutableListOf<Version>()
        val assetsModels = mutableListOf<Asset>()
        for (version in allVersions) {
            versionModels.add(createVersion(version))
            assetsModels.addAll(createAssetsForVersion(version))
        }

        Log.i(
            TAG,
            "Inserting " + versionModels.count() + " versions with " +
                    assetsModels.count() + " assets"
        )
        versionWithAssetsRepository.insertMany(versionModels, assetsModels)
    }

    private fun createVersion(version: Release): Version {
        return Version(
            version.tagName,
            version.name,
            version.body,
            version.prerelease,
            version.publishedAt,
            version.htmlUrl
        )
    }

    private fun createAssetsForVersion(version: Release): List<Asset> {
        val assetsModels = mutableListOf<Asset>()
        for (asset in version.assets) {
            assetsModels.add(
                Asset(
                    version.tagName,
                    asset.name,
                    asset.size,
                    asset.browserDownloadUrl,
                )
            )
        }
        return assetsModels
    }

    companion object {
        const val TAG = "updateService"
    }
}