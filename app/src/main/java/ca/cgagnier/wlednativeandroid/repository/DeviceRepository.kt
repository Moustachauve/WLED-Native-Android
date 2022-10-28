package ca.cgagnier.wlednativeandroid.repository

import android.content.Context
import android.content.SharedPreferences
import ca.cgagnier.wlednativeandroid.DeviceItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object DeviceRepository {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var jsonAdapter: JsonAdapter<Map<String, DeviceItem>>

    private const val SHARED_PREFERENCES_NAME = "WLED_DATA"
    private const val DEVICE_LIST = "WLED_DATA"

    private var devices = HashMap<String, DeviceItem>()
    private var devicesNotHidden: ArrayList<DeviceItem>? = null
    private var listeners = ArrayList<DataChangedListener>()

    interface DataChangedListener {
        fun onItemChanged(item: DeviceItem)
        fun onItemAdded(item: DeviceItem)
        fun onItemRemoved(item: DeviceItem)
    }

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        val type = Types.newParameterizedType(Map::class.java, String::class.java, DeviceItem::class.java)
        val moshi = Moshi.Builder().build()
        jsonAdapter = moshi.adapter(type)

        val devicesJson = sharedPreferences.getString(DEVICE_LIST, "")
        if (devicesJson != null && devicesJson != "") {
            devices = HashMap(jsonAdapter.fromJson(devicesJson) ?: HashMap())
            devicesNotHidden = null
        }
    }

    fun get(address: String): DeviceItem? {
        return devices[address]
    }

    fun getAll(): List<DeviceItem> {
        return ArrayList<DeviceItem>(devices.values)
    }

    fun getAllNotHidden(): ArrayList<DeviceItem> {
        if (devicesNotHidden == null) {
            devicesNotHidden = getAll().filter { !it.isHidden } as ArrayList
        }
        return devicesNotHidden as ArrayList<DeviceItem>
    }

    fun remove(device: DeviceItem) {
        if (!devices.containsKey(device.address)) {
            return
        }
        devices.remove(device.address)
        save()
        devicesNotHidden?.remove(device)
        for (listener in listeners) {
            listener.onItemRemoved(device)
        }
    }

    fun put(device: DeviceItem) {
        val previousDevice = devices.put(device.address, device)
        // TODO maybe dynamically change devicesNotHidden instead of resetting it
        devicesNotHidden = null
        if (previousDevice == null) {
            save()
            for (listener in listeners) {
                listener.onItemAdded(device)
            }
            return
        }

        // Only save to file if at least a field changed
        val needToSave = !previousDevice.isSameForSave(device)
        if (needToSave) {
            save()
        }

        // Only notify if a field changed
        if (needToSave || !previousDevice.isSame(device)) {
            for (listener in listeners) {
                listener.onItemChanged(device)
            }
        }
    }

    fun contains(device: DeviceItem): Boolean = devices.contains(device.address)

    fun registerDataChangedListener(listener: DataChangedListener) {
        listeners.add(listener)
    }

    fun unregisterDataChangedListener(listener: DataChangedListener) {
        listeners.remove(listener)
    }

    private fun save() {
        val devicesJson = jsonAdapter.toJson(devices)
        val prefsEditor = sharedPreferences.edit()
        prefsEditor.putString(DEVICE_LIST, devicesJson)
        prefsEditor.apply()
    }

    fun count() = devices.count()
}