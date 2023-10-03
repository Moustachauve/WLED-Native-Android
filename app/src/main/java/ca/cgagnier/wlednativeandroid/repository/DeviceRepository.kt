package ca.cgagnier.wlednativeandroid.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import ca.cgagnier.wlednativeandroid.model.Device
import kotlinx.coroutines.flow.Flow

class DeviceRepository(deviceDatabase: DevicesDatabase) {
    private val deviceDao = deviceDatabase.deviceDao()
    val allDevices: Flow<List<Device>> = deviceDao.getAlphabetizedDevices()
    val allVisibleDevices: Flow<List<Device>> = deviceDao.getAlphabetizedVisibleDevices()
    val allVisibleDevicesOfflineLast: Flow<List<Device>> = deviceDao.getAlphabetizedVisibleDevicesOfflineLast()

    @WorkerThread
    fun findDevicesWithAddresses(addresses: List<String>): Flow<List<Device>> {
        return deviceDao.findDevicesWithAddresses(addresses)
    }

    @WorkerThread
    suspend fun findDeviceByAddress(address: String): Device? {
        return deviceDao.findDeviceByAddress(address)
    }

    @WorkerThread
    fun findLiveDeviceByAddress(address: String): LiveData<Device> {
        return deviceDao.findLiveDeviceByAddress(address)
    }

    @WorkerThread
    suspend fun findDeviceByMacAddress(address: String): Device? {
        return deviceDao.findDeviceByMacAddress(address)
    }

    @WorkerThread
    suspend fun insert(device: Device) {
        deviceDao.insert(device)
    }

    @WorkerThread
    suspend fun insertMany(devices: List<Device>) {
        deviceDao.insertMany(devices)
    }

    @WorkerThread
    suspend fun update(device: Device) {
        deviceDao.update(device)
    }

    @WorkerThread
    suspend fun delete(device: Device) {
        deviceDao.delete(device)
    }

    fun contains(device: Device): Boolean {
        return deviceDao.count(device.address) > 0
    }
}