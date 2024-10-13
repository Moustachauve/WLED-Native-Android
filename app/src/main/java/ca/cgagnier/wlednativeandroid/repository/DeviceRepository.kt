package ca.cgagnier.wlednativeandroid.repository

import androidx.annotation.WorkerThread
import ca.cgagnier.wlednativeandroid.model.Device
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeviceRepository @Inject constructor(private val deviceDao: DeviceDao) {
    val allDevices: Flow<List<Device>> = deviceDao.getAlphabetizedDevices()
    val allDevicesOfflineLast: Flow<List<Device>> = deviceDao.getAlphabetizedDevicesOfflineLast()

    @WorkerThread
    fun getAllDevices(): List<Device> {
        return deviceDao.getAllDevices()
    }

    @WorkerThread
    fun findDevicesWithAddresses(addresses: List<String>): Flow<List<Device>> {
        return deviceDao.findDevicesWithAddresses(addresses)
    }

    @WorkerThread
    suspend fun findDeviceByAddress(address: String): Device? {
        return deviceDao.findDeviceByAddress(address)
    }

    @WorkerThread
    fun findLiveDeviceByAddress(address: String): Flow<Device?> {
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