package ca.cgagnier.wlednativeandroid.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import ca.cgagnier.wlednativeandroid.DeviceItem
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.Exception

object DeviceRepository {
    private const val TAG = "DEVICE_REPOSITORY"

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var jsonAdapter: JsonAdapter<Map<String, DeviceItem>>

    private const val SHARED_PREFERENCES_NAME = "WLED_DATA"
    private const val DEVICE_LIST = "WLED_DATA"

    private var devices = HashMap<String, DeviceItem>()
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
        if (devicesJson != null) {
            devices = HashMap(jsonAdapter.fromJson(devicesJson) ?: HashMap())
        }
    }

    fun getAll(): List<DeviceItem> {
        return ArrayList<DeviceItem>(devices.values)
    }

    fun remove(device: DeviceItem) {
        if (!devices.containsKey(device.address)) {
            return
        }
        devices.remove(device.address)
        save()
        for (listener in listeners) {
            listener.onItemRemoved(device)
        }
    }

    fun put(device: DeviceItem) {

        val previousDevice = devices.put(device.address, device)
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