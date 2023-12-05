package ca.cgagnier.wlednativeandroid.service.update

import android.content.Context
import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.api.DownloadState
import ca.cgagnier.wlednativeandroid.service.api.github.IllumidelRepoApi
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.File

open class DeviceUpdateService(
    val context: Context,
    val device: Device,
    val versionWithAssets: VersionWithAssets
) {
    protected open val supportedPlatforms = listOf(
        "esp01",
        "esp02",
        "esp32",
        "esp8266"
    )

    protected var couldDetermineAsset: Boolean = false
    protected lateinit var asset: Asset

    open fun getVersionWithPlatformName(): String {
        val ethernetVariant = if (device.isEthernet) "_Ethernet" else ""
        return "WLED_${versionWithAssets.version.tagName.drop(1)}_${device.platformName.uppercase()}${ethernetVariant}.bin"
    }

    open fun determineAsset() {
        if (!supportedPlatforms.contains(device.platformName)) {
            return
        }

        val assetName = getVersionWithPlatformName()
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

    fun getVersionAsset(): Asset {
        return asset
    }

    fun isAssetFileCached(): Boolean {
        return getPathForAsset().exists()
    }

    open suspend fun downloadBinary(): Flow<DownloadState> {
        if (!::asset.isInitialized) {
            throw Exception("Asset could not be determined for ${device.name}.")
        }

        val githubApi = IllumidelRepoApi(context)
        return githubApi.downloadReleaseBinary(asset, getPathForAsset())
    }

    fun installUpdate(): Call<ResponseBody> {
        return DeviceApiService.installUpdate(device, getPathForAsset())
    }

    protected fun getPathForAsset(): File {
        val cacheDirectory = File(context.cacheDir, versionWithAssets.version.tagName)
        cacheDirectory.mkdirs()
        return File(cacheDirectory, asset.name)
    }

    companion object {

    }
}