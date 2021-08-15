package ca.cgagnier.wlednativeandroid.repository

import android.content.Context
import android.content.SharedPreferences
import ca.cgagnier.wlednativeandroid.DeviceListItem
import com.google.gson.Gson

object DeviceRepository {
    private lateinit var sharedPreferences: SharedPreferences

    private const val SHARED_PREFERENCES_NAME = "WLED_DATA"
    private const val DEVICE_LIST = "WLED_DATA"

    private var devices = ArrayList<DeviceListItem>()
    private var listeners = ArrayList<DataChangedListener>()

    interface DataChangedListener {
        fun onItemChanged(index: Int, item: DeviceListItem)
        fun onItemAdded(index: Int, item: DeviceListItem)
        fun onItemRemoved(index: Int)
    }

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        val gson = Gson()
        val devicesJson = sharedPreferences.getString(DEVICE_LIST, "")
        val deviceList = gson.fromJson(devicesJson, Array<DeviceListItem>::class.java)
        if (deviceList != null) {
            devices = ArrayList(deviceList.toList())
        }
    }

    fun add(device: DeviceListItem) {
        devices.add(device)
        save()
        for (listener in listeners) {
            listener.onItemAdded(devices.size - 1, device)
        }
    }

    fun remove(index: Int) {
        devices.removeAt(index)
        save()
        for (listener in listeners) {
            listener.onItemRemoved(index)
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

    operator fun get(index: Int) = devices[index]

    fun count() = devices.count()

}