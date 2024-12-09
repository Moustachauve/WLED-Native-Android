package ca.cgagnier.wlednativeandroid.service.update

import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.api.github.IllumidelRepoApi
import kotlinx.coroutines.flow.Flow
import java.io.File

class IllumidelUpdateService(
    device: Device,
    versionWithAssets: VersionWithAssets,
    cacheDir: File
) : DeviceUpdateService(device, versionWithAssets, cacheDir) {

    override val supportedPlatforms = listOf(
        "esp32",
    )

    override fun determineAsset(): Boolean {
        val langCode = when (device.branch) {
            Branch.BETA -> "en"
            else -> "fr"
        }
        return findAsset("${versionWithAssets.version.tagName}_${device.platformName.uppercase()}_$langCode.bin")
    }

    override suspend fun downloadBinary(): Flow<DownloadState> {
        val githubApi = IllumidelRepoApi(cacheDir)
        return githubApi.downloadReleaseBinary(asset, getPathForAsset())
    }
}