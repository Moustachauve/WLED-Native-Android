package ca.cgagnier.wlednativeandroid.service.update

import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.service.github.Release

class UpdateService {
    fun checkForUpdate(device: Device) {
        val release = Release()
        val latest = release.getLatestRelease()
    }
}