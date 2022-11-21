package ca.cgagnier.wlednativeandroid

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.databinding.ActivityMainBinding
import ca.cgagnier.wlednativeandroid.repository.ThemeSettings
import ca.cgagnier.wlednativeandroid.repository_v0.DataMigrationV0toV1
import ca.cgagnier.wlednativeandroid.service.DeviceApi
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            val devicesApp = (application as DevicesApplication)
            devicesApp.userPreferencesRepository.themeMode.collect {
                setThemeMode(it)
            }
        }

        super.onCreate(savedInstanceState)
        DeviceApi.setApplication(application as DevicesApplication)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainerView) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
                rightMargin = insets.right
            }

            windowInsets
        }

        checkMigration()

        var isConnectedToWledAP: Boolean
        try {
            isConnectedToWledAP = DeviceDiscovery.isConnectedToWledAP(applicationContext)
        } catch (e: Exception) {
            isConnectedToWledAP = false
            Log.e(TAG, "Error when checking isConnectedToWledAP: " + e.message, e)
        }

        if (isConnectedToWledAP) {
            val connectionManager =
                applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?

            val request = NetworkRequest.Builder()
            request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

            connectionManager!!.requestNetwork(
                request.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        try {
                            connectionManager.bindProcessToNetwork(network)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }

        setContentView(binding.root)
    }

    private fun checkMigration() {
        lifecycleScope.launch {
            val devicesApp = (application as DevicesApplication)
            val userPreferences = devicesApp.userPreferencesRepository.fetchInitialPreferences()
            if (!userPreferences.hasMigratedSharedPref) {
                Log.i(TAG, "Starting devices migration from V0 to V1")
                DataMigrationV0toV1(applicationContext, devicesApp.repository).migrate()
                devicesApp.userPreferencesRepository.updatehasMigratedSharedPref(true)
                Log.i(TAG, "Migration done.")
            }
        }
    }

    private fun setThemeMode(theme: ThemeSettings){
        val mode = when(theme){
            ThemeSettings.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeSettings.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }
}