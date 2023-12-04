package ca.cgagnier.wlednativeandroid.service.update

import android.content.Context
import android.util.Log
import ca.cgagnier.wlednativeandroid.fragment.DeviceListFragment
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.model.githubapi.Release
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.api.github.IllumidelRepoApi
import com.vdurmont.semver4j.Semver


class ReleaseService(private val versionWithAssetsRepository: VersionWithAssetsRepository) {

    /**
     * If a new version is available, returns the version tag of it.
     *
     * @param versionName Current version to check if a newer one exists
     * @param branch Which branch to check for the update
     * @param ignoreVersion You can specify a version tag to be ignored as a new version. If this is
     *      set and match with the newest version, no version will be returned
     * @return The newest version if it is newer than versionName and different than ignoreVersion,
     *      otherwise an empty string.
     */
    suspend fun getNewerReleaseTag(versionName: String, branch: Branch, ignoreVersion: String): String {
        if (versionName == Device.UNKNOWN_VALUE) {
            return ""
        }
        val latestVersion = getLatestVersionWithAssets(branch) ?: return ""
        if (latestVersion.version.tagName == ignoreVersion) {
            return ""
        }

        try {
            return if (Semver(
                    latestVersion.version.tagName.drop(1),
                    Semver.SemverType.LOOSE
                ).isGreaterThan(versionName)
            ) {
                latestVersion.version.tagName
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(DeviceListFragment.TAG, "Error in getNewerReleaseTag: " + e.message, e)
        }

        return ""
    }

    suspend fun getLatestVersionWithAssets(branch: Branch): VersionWithAssets? {
        if (branch == Branch.BETA) {
            return versionWithAssetsRepository.getLatestBetaVersionWithAssets()
        }

        return versionWithAssetsRepository.getLatestStableVersionWithAssets()
    }

    suspend fun refreshVersions(context: Context) {
        val allVersions = IllumidelRepoApi(context).getAllReleases()

        if (allVersions == null) {
            Log.w(TAG, "Did not find any version")
            return
        }

        versionWithAssetsRepository.removeAll()

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
                    asset.id,
                )
            )
        }
        return assetsModels
    }

    companion object {
        const val TAG = "updateService"
    }
}