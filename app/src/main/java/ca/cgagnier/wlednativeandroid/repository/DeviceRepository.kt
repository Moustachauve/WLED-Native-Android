package ca.cgagnier.wlednativeandroid.repository

import androidx.annotation.WorkerThread
import ca.cgagnier.wlednativeandroid.model.Device
import kotlinx.coroutines.flow.Flow

class DeviceRepository(deviceDatabase: DevicesDatabase) {
    private val deviceDao = deviceDatabase.deviceDao()
    val allDevices: Flow<List<Device>> = deviceDao.getAlphabetizedDevices()
    val allVisibleDevices: Flow<List<Device>> = deviceDao.getAlphabetizedVisibleDevices()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun findDevicesWithAddresses(addresses: List<String>): Flow<List<Device>> {
        return deviceDao.findDevicesWithAddresses(addresses)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun findDeviceByAddress(address: String): Device? {
        return deviceDao.findDeviceByAddress(address)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(device: Device) {
        deviceDao.insert(device)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertMany(devices: List<Device>) {
        deviceDao.insertMany(devices)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(device: Device) {
        deviceDao.update(device)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(device: Device) {
        deviceDao.delete(device)
    }

    fun contains(device: Device): Boolean {
        return deviceDao.count(device.address) > 0
    }
}