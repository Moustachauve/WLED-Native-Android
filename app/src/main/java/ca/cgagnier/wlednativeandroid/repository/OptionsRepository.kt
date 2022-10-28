package ca.cgagnier.wlednativeandroid.repository

import android.content.Context
import android.content.SharedPreferences
import ca.cgagnier.wlednativeandroid.Options
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

object OptionsRepository {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var jsonAdapter: JsonAdapter<Options>

    private const val SHARED_PREFERENCES_NAME = "WLED_DATA"
    private const val DATA_OPTIONS_KEY = "WLED_OPTIONS"

    private const val CURRENT_VERSION = 1

    private lateinit var options: Options

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        val moshi = Moshi.Builder().build()
        jsonAdapter = moshi.adapter(Options::class.java)

        val optionsJson = sharedPreferences.getString(DATA_OPTIONS_KEY, "")
        if (optionsJson != null && optionsJson != "") {
            options = jsonAdapter.fromJson(optionsJson) ?: Options(CURRENT_VERSION, 0)
        } else {
            options = Options(CURRENT_VERSION, 0)
        }
    }

    // Get the options for the app. Do not store this variable as it might get out of sync.
    fun get(): Options {
        return options
    }

    fun save(newOptions: Options) {
        if (options == newOptions) {
            return
        }

        options = newOptions
        val optionsJson = jsonAdapter.toJson(options)
        val prefsEditor = sharedPreferences.edit()
        prefsEditor.putString(DATA_OPTIONS_KEY, optionsJson)
        prefsEditor.apply()
    }

    fun saveSelectedIndex(selectedIndex: Int) {
        save(options.copy(lastSelectedIndex = selectedIndex))
    }
}