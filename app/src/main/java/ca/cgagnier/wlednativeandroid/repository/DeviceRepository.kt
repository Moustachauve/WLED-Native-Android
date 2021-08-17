package ca.cgagnier.wlednativeandroid.repository

import android.content.Context
import android.content.SharedPreferences
import ca.cgagnier.wlednativeandroid.DeviceItem
import com.google.gson.Gson

object DeviceRepository {
    private lateinit var sharedPreferences: SharedPreferences

    private const val SHARED_PREFERENCES_NAME = "WLED_DATA"
    private const val DEVICE_LIST = "WLED_DATA"

    private var devices = HashSet<DeviceItem>()
    private var listeners = ArrayList<DataChangedListener>()

    interface DataChangedListener {
        fun onItemChanged(item: DeviceItem)
        fun onItemAdded(item: DeviceItem)
        fun onItemRemoved(item: DeviceItem)
    }

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        val gson = Gson()
        val devicesJson = sharedPreferences.getString(DEVICE_LIST, "")
        val deviceList = gson.fromJson(devicesJson, Array<DeviceItem>::class.java)
        if (deviceList != null) {
            devices = deviceList.toHashSet()
        }
    }

    fun getAll(): List<DeviceItem> {
        return devices.toList()
    }

    fun add(device: DeviceItem) {
        devices.add(device)
        save()
        for (listener in listeners) {
            listener.onItemAdded(device)
        }
    }

    fun remove(device: DeviceItem) {
        devices.remove(device)
        save()
        for (listener in listeners) {
            listener.onItemRemoved(device)
        }
    }

    fun registerDataChangedListener(listener: DataChangedListener) {
        listeners.add(listener)
    }

    fun unregisterDataChangedListener(listener: DataChangedListener) {
        listeners.remove(listener)
    }

    private fun save() {
        val gson = Gson()
        val devicesJson = gson.toJson(devices)

        val prefsEditor = sharedPreferences.edit()
        prefsEditor.putString(DEVICE_LIST, devicesJson)
        prefsEditor.apply()
    }

    fun count() = devices.count()
}