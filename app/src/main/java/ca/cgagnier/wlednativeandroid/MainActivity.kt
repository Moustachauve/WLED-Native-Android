package ca.cgagnier.wlednativeandroid

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import ca.cgagnier.wlednativeandroid.databinding.ActivityMainBinding
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.ThemeSettings
import ca.cgagnier.wlednativeandroid.repository_v0.DataMigrationV0toV1
import ca.cgagnier.wlednativeandroid.service.DeviceApiService
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import ca.cgagnier.wlednativeandroid.service.update.ReleaseService
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModel
import ca.cgagnier.wlednativeandroid.viewmodel.DeviceListViewModelFactory
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AutoDiscoveryActivity, DeviceDiscovery.DeviceDiscoveredListener,
    AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val autoDiscoveryLoopHandler = Handler(Looper.getMainLooper())
    private val deviceListViewModel: DeviceListViewModel by viewModels {
        DeviceListViewModelFactory(
            (application as DevicesApplication).deviceRepository,
            (application as DevicesApplication).userPreferencesRepository
        )
    }

    private var isAutoDiscoveryEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val devicesApp = (application as DevicesApplication)
        checkMigration()
        lifecycleScope.launch {
            devicesApp.userPreferencesRepository.themeMode.collect {
                setThemeMode(it)
            }
        }
        lifecycleScope.launch {
            devicesApp.userPreferencesRepository.sendCrashData.collect {
                Log.i(TAG, "Setting crashData to $it")
                Firebase.crashlytics.setCrashlyticsCollectionEnabled(it)
            }
        }
        lifecycleScope.launch {
            devicesApp.userPreferencesRepository.sendCrashData.collect {
                Log.i(TAG, "Setting performance data to $it")
                Firebase.performance.isPerformanceCollectionEnabled = it
            }
        }

        super.onCreate(savedInstanceState)
        DeviceApiService.setApplication(application as DevicesApplication)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainerView) { insetView, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val insetsIme = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            insetView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom + insetsIme.bottom
                rightMargin = insets.right
            }

            windowInsets
        }

        lifecycleScope.launch {
            devicesApp.userPreferencesRepository.autoDiscovery.collect {
                isAutoDiscoveryEnabled = it
                if (isAutoDiscoveryEnabled) {
                    startAutoDiscovery()
                } else {
                    stopAutoDiscovery()
                }
            }
        }
        updateDeviceVersionList()
        setContentView(binding.root)
    }

    override fun onResume() {
        startAutoDiscovery()
        super.onResume()
    }

    override fun onPause() {
        stopAutoDiscovery()
        super.onPause()
    }

    override fun startAutoDiscovery() {
        if (!isAutoDiscoveryEnabled) {
            Log.i(TAG, "Auto discovery is not enabled")
            return
        }
        Log.i(TAG, "Starting auto discovery")
        (application as DevicesApplication).deviceDiscovery
            .registerDeviceDiscoveredListener(this)
        (application as DevicesApplication).deviceDiscovery.start()
        autoDiscoveryLoopHandler.postDelayed({ stopAutoDiscovery() }, 25000)
    }

    override fun stopAutoDiscovery() {
        Log.i(TAG, "Stopping auto discovery")
        autoDiscoveryLoopHandler.removeCallbacksAndMessages(null)
        (application as DevicesApplication).deviceDiscovery
            .unregisterDeviceDiscoveredListener(this)
        (application as DevicesApplication).deviceDiscovery.stop()
    }

    private fun checkMigration() {
        lifecycleScope.launch {
            val devicesApp = (application as DevicesApplication)
            val userPreferences = devicesApp.userPreferencesRepository.fetchInitialPreferences()
            if (!userPreferences.hasMigratedSharedPref) {
                Log.i(TAG, "Starting devices migration from V0 to V1")
                DataMigrationV0toV1(applicationContext, devicesApp.deviceRepository).migrate()
                devicesApp.userPreferencesRepository.updateHasMigratedSharedPref(true)
                Log.i(TAG, "Migration done.")
            }
        }
    }


    override fun onDeviceDiscovered(serviceInfo: NsdServiceInfo) {
        Log.i(TAG, "Device discovered!")
        @Suppress("DEPRECATION")
        val deviceIp = serviceInfo.host.hostAddress!!
        val deviceName = serviceInfo.serviceName ?: ""
        val device = Device(
            deviceIp, deviceName,
            isCustomName = false,
            isHidden = false,
            macAddress = Device.UNKNOWN_VALUE
        )

        if (deviceListViewModel.contains(device)) {
            Log.i(TAG, "Device already exists")
            return
        }
        Log.i(TAG, "IP: ${deviceIp}\tName: ${deviceName}\t")

        DeviceApiService.update(device, silentUpdate = true, saveChanges = false) { refreshedDevice ->
            lifecycleScope.launch {
                val existingDevice = deviceListViewModel.findWithSameMacAddress(refreshedDevice)
                if (existingDevice != null && refreshedDevice.macAddress != Device.UNKNOWN_VALUE) {
                    Log.i(
                        TAG,
                        "Device ${existingDevice.address} already exists with the same mac address ${existingDevice.macAddress}"
                    )
                    val refreshedExistingDevice = existingDevice.copy(
                        address = refreshedDevice.address,
                        isOnline = refreshedDevice.isOnline,
                        name = refreshedDevice.name,
                        brightness = refreshedDevice.brightness,
                        isPoweredOn = refreshedDevice.isPoweredOn,
                        color = refreshedDevice.color,
                        networkRssi = refreshedDevice.networkRssi,
                        isEthernet = refreshedDevice.isEthernet,
                        platformName = refreshedDevice.platformName,
                        version = refreshedDevice.version,
                        brand = refreshedDevice.brand,
                        productName = refreshedDevice.productName,
                    )
                    deviceListViewModel.delete(existingDevice)
                    deviceListViewModel.insert(refreshedExistingDevice)
                } else {
                    deviceListViewModel.insert(refreshedDevice)
                }
            }
        }
    }

    private fun setThemeMode(theme: ThemeSettings) {
        val mode = when (theme) {
            ThemeSettings.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeSettings.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Checks for device updates once in a while
     */
    private fun updateDeviceVersionList() {
        lifecycleScope.launch(Dispatchers.IO) {
            val app = (application as DevicesApplication)
            app.userPreferencesRepository.lastUpdateCheckDate.collect {
                val now = System.currentTimeMillis()
                if (now < it) {
                    Log.i(TAG, "Not updating version list since it was done recently.")
                    return@collect
                }
                val releaseService = ReleaseService(app.versionWithAssetsRepository)
                releaseService.refreshVersions(applicationContext)
                // Set the next date to check in minimum 24 hours from now.
                app.userPreferencesRepository.updateLastUpdateCheckDate(now + (24 * 60 * 60 * 1000))
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }
}