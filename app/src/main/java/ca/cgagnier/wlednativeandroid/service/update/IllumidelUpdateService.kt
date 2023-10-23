package ca.cgagnier.wlednativeandroid.service.update

import android.content.Context
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

    override fun determineAsset() {
        val assetName = "${versionWithAssets.version.tagName}.bin"
        for (asset in versionWithAssets.assets) {
            if (asset.name == assetName) {
                this.asset = asset
                couldDetermineAsset = true
                return
            }
        }
    }

    override suspend fun downloadBinary(): Flow<DownloadState> {
        val githubApi = IllumidelRepoApi(context)
        return githubApi.downloadReleaseBinary(asset, getPathForAsset())
    }
}