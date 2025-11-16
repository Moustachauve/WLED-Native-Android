package ca.cgagnier.wlednativeandroid.repository

import androidx.annotation.WorkerThread
import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeviceRepository @Inject constructor(private val deviceDao: StatefulDeviceDao) {
    val allDevices: Flow<List<StatefulDevice>> = deviceDao.getAlphabetizedDevices()
    val allDevicesOfflineLast: Flow<List<StatefulDevice>> = deviceDao.getAlphabetizedDevicesOfflineLast()

    @WorkerThread
    fun getAllDevices(): List<StatefulDevice> {
        return deviceDao.getAllDevices()
    }

    @WorkerThread
    fun findLiveDeviceByAddress(address: String): Flow<StatefulDevice?> {
        return deviceDao.findLiveDeviceByAddress(address)
    }

    @WorkerThread
    suspend fun findDeviceByMacAddress(address: String): StatefulDevice? {
        return deviceDao.findDeviceByMacAddress(address)
    }

    @WorkerThread
    suspend fun insert(device: StatefulDevice) {
        deviceDao.insert(device)
    }

    @WorkerThread
    suspend fun update(device: StatefulDevice) {
        deviceDao.update(device)
    }

    @WorkerThread
    suspend fun delete(device: StatefulDevice) {
        deviceDao.delete(device)
    }

    fun contains(device: StatefulDevice): Boolean {
        return deviceDao.count(device.address) > 0
    }

    suspend fun hasHiddenDevices(): Boolean {
        return deviceDao.countHiddenDevices() > 0
    }
}