package ca.cgagnier.wlednativeandroid.service.update

import android.content.Context
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.api.github.GithubApi
import kotlinx.coroutines.flow.Flow

class DeviceUpdateService(
    val context: Context,
    val device: Device,
    val versionWithAssets: VersionWithAssets
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

    private fun determineAsset() {
        if (!supportedPlatforms.contains(device.platformName)) {
            return
        }

        val ethernetVariant = if (device.isEthernet) "_Ethernet" else ""
        val version = versionWithAssets.version.tagName.drop(1)
        val assetName = "WLED_${version}_${device.platformName.uppercase()}${ethernetVariant}.bin"
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

    fun getAsset(): Asset {
        return asset
    }

    suspend fun downloadBinary(): Flow<DownloadState> {
        if (!::asset.isInitialized) {
            throw Exception("Asset could not be determined for ${device.name}.")
        }

        val githubApi = GithubApi(context)
        // TODO get from caching if already exists
        return githubApi.downloadReleaseBinary(versionWithAssets.version, asset)
    }

    companion object {

    }
}