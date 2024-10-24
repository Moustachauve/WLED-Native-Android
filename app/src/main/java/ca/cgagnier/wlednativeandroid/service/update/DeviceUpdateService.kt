package ca.cgagnier.wlednativeandroid.service.update

import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.api.github.GithubApi
import kotlinx.coroutines.flow.Flow
import java.io.File

class DeviceUpdateService(
    val device: Device,
    private val versionWithAssets: VersionWithAssets,
    private val cacheDir: File
) {
    private val supportedPlatforms = listOf(
        "esp01",
        "esp02",
        "esp32",
        "esp8266"
    )

    private var couldDetermineAsset: Boolean = false
    private lateinit var asset: Asset

    init {
        determineAsset()
    }

    fun getVersionWithPlatformName(): String {
        val ethernetVariant = if (device.isEthernet) "_Ethernet" else ""
        return "${versionWithAssets.version.tagName}_${device.platformName.uppercase()}${ethernetVariant}"
    }

    private fun determineAsset() {
        if (!supportedPlatforms.contains(device.platformName)) {
            return
        }

        val versionWithPlatform = getVersionWithPlatformName().drop(1)
        val assetName = "WLED_${versionWithPlatform}.bin"
        for (asset in versionWithAssets.assets) {
            if (asset.name == assetName) {
                this.asset = asset
                couldDetermineAsset = true
                return
            }
        }
    }

    fun couldDetermineAsset(): Boolean {
        return couldDetermineAsset
    }

    fun isAssetFileCached(): Boolean {
        return getPathForAsset().exists()
    }

    suspend fun downloadBinary(): Flow<DownloadState> {
        if (!::asset.isInitialized) {
            throw Exception("Asset could not be determined for ${device.name}.")
        }

        val githubApi = GithubApi(cacheDir)
        return githubApi.downloadReleaseBinary(asset, getPathForAsset())
    }

    fun getPathForAsset(): File {
        val cacheDirectory = File(cacheDir, versionWithAssets.version.tagName)
        cacheDirectory.mkdirs()
        return File(cacheDirectory, asset.name)
    }

    companion object {

    }
}