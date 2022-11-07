package ca.cgagnier.wlednativeandroid.repository_v0

import android.content.Context
import android.content.SharedPreferences
import ca.cgagnier.wlednativeandroid.model.legacy.DeviceItem
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

    fun getAll(): List<DeviceItem> {
        return ArrayList<DeviceItem>(devices.values)
    }

    fun clearStorage() {
        val prefsEditor = sharedPreferences.edit()
        prefsEditor.remove(DEVICE_LIST)
        prefsEditor.apply()
    }
}