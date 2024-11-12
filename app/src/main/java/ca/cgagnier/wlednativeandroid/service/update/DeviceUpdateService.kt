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

    private var assetName: String = ""
    private var couldDetermineAsset: Boolean = false
    private lateinit var asset: Asset

    init {
        // Try to use the release variable, but fallback to the legacy platform method for
        // compatibility with WLED older than 0.15.0
        if (!determineAssetByRelease()) {
            determineAssetByPlatform()
        }
    }

    // Preferred method, only available since WLED 0.15.0
    private fun determineAssetByRelease(): Boolean {
        if (device.release.isEmpty() || device.release == Device.UNKNOWN_VALUE) {
            return false
        }

        val versionWithRelease = "${versionWithAssets.version.tagName}_${device.release}".drop(1)
        assetName = "WLED_${versionWithRelease}.bin"
        return findAsset(assetName)
    }

    // Legacy method for backwards compatibility with WLED older than 0.15.0
    private fun determineAssetByPlatform(): Boolean {
        if (!supportedPlatforms.contains(device.platformName)) {
            return false
        }

        val ethernetVariant = if (device.isEthernet) "_Ethernet" else ""
        val versionWithPlatform = "${versionWithAssets.version.tagName}_${device.platformName.uppercase()}".drop(1)
        assetName = "WLED_${versionWithPlatform}${ethernetVariant}.bin"
        return findAsset(assetName)
    }

    private fun findAsset(assetName: String): Boolean {
        for (asset in versionWithAssets.assets) {
            if (asset.name == assetName) {
                this.asset = asset
                couldDetermineAsset = true
                return true
            }
        }
        return false
    }

    /**
     * Get the name of the asset to download.
     */
    fun getAssetName(): String {
        return assetName
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
}