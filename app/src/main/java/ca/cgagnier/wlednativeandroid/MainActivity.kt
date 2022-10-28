package ca.cgagnier.wlednativeandroid

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.fragment.app.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import ca.cgagnier.wlednativeandroid.databinding.ActivityMainBinding
import ca.cgagnier.wlednativeandroid.fragment.DeviceAddManuallyFragment
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.repository.OptionsRepository
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery


class MainActivity : AppCompatActivity(),
    FragmentManager.OnBackStackChangedListener,
    DeviceAddManuallyFragment.NoticeDialogListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        //binding.mainToolbar.setupWithNavController(navController, appBarConfiguration)

        initDevices()

        //setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportFragmentManager.addOnBackStackChangedListener(this)

        updateIsBackArrowVisible()

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

    override fun onBackStackChanged() {
        updateIsBackArrowVisible()
    }

    fun switchContent(id: Int, fragment: Fragment) {
        switchContent(id, fragment, fragment.toString())
    }

    fun switchContent(id: Int, fragment: Fragment, tag: String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(id, fragment, tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDeviceManuallyAdded(dialog: DialogFragment) {
        supportFragmentManager.popBackStackImmediate()
    }

    private fun initDevices() {
        DeviceRepository.init(applicationContext)
        OptionsRepository.init(applicationContext)
    }

    private fun updateIsBackArrowVisible() {
        supportActionBar?.setDisplayHomeAsUpEnabled(
            supportFragmentManager.backStackEntryCount > 0
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }
}