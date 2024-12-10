package ca.cgagnier.wlednativeandroid.service.update

import android.util.Log
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Version
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.model.githubapi.Release
import ca.cgagnier.wlednativeandroid.model.wledapi.Info
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import ca.cgagnier.wlednativeandroid.service.api.github.GithubApi
import com.vdurmont.semver4j.Semver
import java.io.File

private const val TAG = "updateService"
private const val WLED_BRAND = "WLED"
private const val WLED_PRODUCT = "FOSS"

class ReleaseService(private val versionWithAssetsRepository: VersionWithAssetsRepository) {

    /**
     * If a new version is available, returns the version tag of it.
     *
     * @param deviceInfo Latest information about the device
     * @param branch Which branch to check for the update
     * @param ignoreVersion You can specify a version tag to be ignored as a new version. If this is
     *      set and match with the newest version, no version will be returned
     * @return The newest version if it is newer than versionName and different than ignoreVersion,
     *      otherwise an empty string.
     */
    suspend fun getNewerReleaseTag(deviceInfo: Info, branch: Branch, ignoreVersion: String): String {
        if (deviceInfo.version.isNullOrEmpty()) {
            return ""
        }
        // This would need some major refactoring in order to support different sources for OTA.
        if (deviceInfo.brand != WLED_BRAND || deviceInfo.product != WLED_PRODUCT) {
            return ""
        }

        // The options bitmask at 0x01 being 0 means OTA is disabled on the device.
        if (deviceInfo.options?.and(0x01) == 0) {
            return ""
        }

        val latestVersion = getLatestVersionWithAssets(branch) ?: return ""
        if (latestVersion.version.tagName == ignoreVersion) {
            return ""
        }

        val betaSuffixes = listOf("-a", "-b", "-rc")
        Log.w(TAG, "Device ${deviceInfo.ipAddress}: ${deviceInfo.version} to ${latestVersion.version.tagName}")
        if (branch == Branch.STABLE && betaSuffixes.any { deviceInfo.version.contains(it, ignoreCase = true)}) {
            // If we're on a beta branch but looking for a stable branch, always offer to "update" to
            // the stable branch.
            return latestVersion.version.tagName
        } else if (branch == Branch.BETA && betaSuffixes.none { deviceInfo.version.contains(it, ignoreCase = true)}) {
            // Same if we are on a stable branch but looking for a beta branch, we should offer to
            // "update" to the latest beta branch, even if its older.
            return latestVersion.version.tagName
        }

        try {
            return if (Semver(
                    latestVersion.version.tagName.drop(1),
                    Semver.SemverType.LOOSE
                ).isGreaterThan(deviceInfo.version)
            ) {
                latestVersion.version.tagName
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getNewerReleaseTag: " + e.message, e)
        }

        return ""
    }

    private suspend fun getLatestVersionWithAssets(branch: Branch): VersionWithAssets? {
        if (branch == Branch.BETA) {
            return versionWithAssetsRepository.getLatestBetaVersionWithAssets()
        }

        return versionWithAssetsRepository.getLatestStableVersionWithAssets()
    }

    suspend fun refreshVersions(cacheDir: File) {
        val allVersions = GithubApi(cacheDir).getAllReleases()

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
}