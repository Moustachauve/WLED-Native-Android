package ca.cgagnier.wlednativeandroid.service.update

import ca.cgagnier.wlednativeandroid.model.Asset
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.Version

class DeviceUpdateService(val device: Device, val version: Version) {
    val supportedPlatforms = listOf(
        "esp01",
        "esp02",
        "esp32",
        "esp8266"
    )

    var canDetermineAsset: Boolean = false
    var determinatedAsset: Asset? = null

    init {
        determineAsset()
    }

    private fun determineAsset() {
        if (!supportedPlatforms.contains(device.platformName)) {
            return
        }

    }

    companion object {

    }
}