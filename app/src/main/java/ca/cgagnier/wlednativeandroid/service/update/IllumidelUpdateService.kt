package ca.cgagnier.wlednativeandroid.service.update

import android.content.Context
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.api.github.IllumidelRepoApi
import kotlinx.coroutines.flow.Flow

class IllumidelUpdateService(
    context: Context,
    device: Device,
    versionWithAssets: VersionWithAssets
) : DeviceUpdateService(context, device, versionWithAssets) {

    override val supportedPlatforms = listOf(
        "esp32",
    )

    override fun getVersionWithPlatformName(): String {
        val langCode = when (device.branch) {
            Branch.BETA -> "en"
            else -> "fr"
        }
        return "${versionWithAssets.version.tagName}_${device.platformName.uppercase()}_$langCode.bin"
    }

    override suspend fun downloadBinary(): Flow<DownloadState> {
        val githubApi = IllumidelRepoApi(context)
        return githubApi.downloadReleaseBinary(asset, getPathForAsset())
    }
}