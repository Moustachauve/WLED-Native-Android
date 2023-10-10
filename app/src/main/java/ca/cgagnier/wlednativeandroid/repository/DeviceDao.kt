package ca.cgagnier.wlednativeandroid.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import ca.cgagnier.wlednativeandroid.model.Device
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: Device)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(device: List<Device>)

    @Update
    suspend fun update(device: Device)

    @Delete
    suspend fun delete(device: Device)

    @Query("DELETE FROM device")
    suspend fun deleteAll()

    @Query("SELECT * FROM device WHERE address = :address")
    suspend fun findDeviceByAddress(address: String): Device?

    @Query("SELECT * FROM device WHERE address = :address")
    fun findLiveDeviceByAddress(address: String): Flow<Device>

    @Query("SELECT * FROM device WHERE macAddress != '' AND macAddress = :address")
    suspend fun findDeviceByMacAddress(address: String): Device?

    @Query("SELECT * FROM device WHERE address IN (:addresses)")
    fun findDevicesWithAddresses(addresses: List<String>): Flow<List<Device>>

    @Query("SELECT COUNT() FROM device WHERE address = :address")
    fun count(address: String): Int

    @RawQuery
    suspend fun insert(query: SupportSQLiteQuery): Device

    @Query("SELECT * FROM Device ORDER BY LOWER(name) ASC, LOWER(address) ASC")
    fun getAlphabetizedDevices(): Flow<List<Device>>

    @Query("SELECT * FROM Device WHERE isHidden == 0 ORDER BY LOWER(name) ASC, LOWER(address) ASC")
    fun getAlphabetizedVisibleDevices(): Flow<List<Device>>

    @Query("SELECT * FROM Device WHERE isHidden == 0 ORDER BY isOnline DESC, LOWER(name) ASC, LOWER(address) ASC")
    fun getAlphabetizedVisibleDevicesOfflineLast(): Flow<List<Device>>

    @Query("SELECT * FROM Device WHERE isHidden == 0 ORDER BY isOnline DESC, LOWER(name) ASC, LOWER(address) ASC LIMIT 1")
    fun getFirstVisibleDeviceOfflineLast(): Flow<Device?>
}