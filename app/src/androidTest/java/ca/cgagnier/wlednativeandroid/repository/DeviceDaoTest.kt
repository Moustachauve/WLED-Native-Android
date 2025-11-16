package ca.cgagnier.wlednativeandroid.repository


import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DeviceDaoTest {

    private lateinit var deviceDao: StatefulDeviceDao
    private lateinit var db: DevicesDatabase

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, DevicesDatabase::class.java)
        // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
                .build()
        deviceDao = db.statefulDeviceDao()
    }
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetDevice() = runBlocking {
        val device = StatefulDevice(
            address = "address",
            name = "name",
            isCustomName = true,
            isHidden = true,
            macAddress = "123"
        )
        deviceDao.insert(device)
        val allDevices = deviceDao.getAlphabetizedDevices().first()
        assertEquals(allDevices[0].address, device.address)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetDeviceByAddress() = runBlocking {
        val device = StatefulDevice(
            address = "address1",
            name = "name",
            isCustomName = true,
            isHidden = true,
            macAddress = "123"
        )
        deviceDao.insert(device)
        val device2 = StatefulDevice(
            address = "address2",
            name = "name",
            isCustomName = true,
            isHidden = true,
            macAddress = "456"
        )
        deviceDao.insert(device2)
        val deviceByAddress = deviceDao.findDeviceByAddress(device.address)
        val device2ByAddress = deviceDao.findDeviceByAddress(device2.address)
        assertEquals(deviceByAddress?.address ?: "", device.address)
        assertEquals(device2ByAddress?.address ?: "", device2.address)
    }

    @Test
    @Throws(Exception::class)
    fun getAllDevices() = runBlocking {
        val device = StatefulDevice(
            "address1",
            "name1",
            isCustomName = true,
            true,
            macAddress = "123"
        )
        deviceDao.insert(device)
        val device2 = StatefulDevice(
            "address2",
            "name2",
            isCustomName = true,
            false,
            macAddress = "456"
        )
        deviceDao.insert(device2)
        val allDevices = deviceDao.getAlphabetizedDevices().first()
        assertEquals(allDevices[0].address, device.address)
        assertEquals(allDevices[1].address, device2.address)
    }
    
    @Test
    @Throws(Exception::class)
    fun deleteAll() = runBlocking {
        val device = StatefulDevice(
            address = "address10",
            name = "name10",
            isCustomName = true,
            isHidden = true,
            macAddress = "123"
        )
        deviceDao.insert(device)
        val device2 = StatefulDevice(
            address = "address12",
            name = "name12",
            isCustomName = true,
            isHidden = true,
            macAddress = "456"
        )
        deviceDao.insert(device2)
        deviceDao.deleteAll()
        val allDevices = deviceDao.getAlphabetizedDevices().first()
        assertTrue(allDevices.isEmpty())
    }
}