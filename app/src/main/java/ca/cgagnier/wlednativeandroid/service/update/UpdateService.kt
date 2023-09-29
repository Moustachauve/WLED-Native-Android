package ca.cgagnier.wlednativeandroid.service.update

import android.content.Context
import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.api.github.GithubApi
import com.vdurmont.semver4j.Semver
import org.kohsuke.github.GHRelease


class UpdateService(private val versionWithAssetsRepository: VersionWithAssetsRepository) {

    suspend fun hasUpdateAvailable(versionName: String): Boolean {
        if (versionName == Device.UNKNOWN_VALUE) {
            return false
        }
        val latestVersion = versionWithAssetsRepository.getLatestVersionWithAssets() ?: return false
        return Semver(latestVersion.version.tagName.drop(1)).isGreaterThan(versionName)
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

    private fun createVersion(version: GHRelease): Version {
        return Version(
            version.tagName,
            version.name,
            version.body,
            version.isPrerelease,
            version.published_at,
            version.htmlUrl.toString()
        )
    }

    private fun createAssetsForVersion(version: GHRelease): List<Asset> {
        val allAssets = version.listAssets()
        val assetsModels = mutableListOf<Asset>()
        for (asset in allAssets) {
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