package ca.cgagnier.wlednativeandroid.repository_v0

import android.content.Context
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository as DeviceRepositoryV1

class DataMigrationV0toV1(val context: Context, val repositoryV1: DeviceRepositoryV1) {
    suspend fun migrate() {
        DeviceRepository.init(context)

        val allDeviceItemsV0 = DeviceRepository.getAll()
        if (allDeviceItemsV0.isEmpty()) {
            return
        }

        val allDevicesV1 = ArrayList<Device>()

        for (deviceItem in allDeviceItemsV0) {
            allDevicesV1.add(Device(
                address = deviceItem.address,
                name = deviceItem.name,
                isCustomName = deviceItem.isCustomName,
                isHidden = deviceItem.isHidden,
                macAddress = "",
                brightness  = deviceItem.brightness,
                color  = deviceItem.color,
                isPoweredOn  = deviceItem.isPoweredOn,
                isOnline  = deviceItem.isOnline,
                isRefreshing  = deviceItem.isRefreshing,
                networkRssi  = deviceItem.networkRssi
            ))
        }

        repositoryV1.insertMany(allDevicesV1)
        DeviceRepository.clearStorage()
    }
}