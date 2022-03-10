package ca.cgagnier.wlednativeandroid

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.*
import ca.cgagnier.wlednativeandroid.fragment.DeviceAddManuallyFragment
import ca.cgagnier.wlednativeandroid.fragment.DeviceListFragment
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.fragment.DeviceViewFragment
import ca.cgagnier.wlednativeandroid.service.DeviceDiscovery
import java.lang.Exception


class MainActivity : AppCompatActivity(R.layout.activity_main),
    FragmentManager.OnBackStackChangedListener,
    DeviceAddManuallyFragment.NoticeDialogListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDevices()

        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportFragmentManager.addOnBackStackChangedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<DeviceListFragment>(R.id.fragment_container_view)
            }
        }

        updateIsBackArrowVisible()

        var isConnectedToWledAP = false
        try {
            isConnectedToWledAP = DeviceDiscovery.isConnectedToWledAP(applicationContext)
        } catch (e: Exception) {
            isConnectedToWledAP = false
            Log.e(TAG, "Error when checking isConnectedToWledAP: " + e.message, e)
        }

        if (isConnectedToWledAP) {
            val connectionManager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager?

            val request = NetworkRequest.Builder()
            request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

            connectionManager!!.requestNetwork(request.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    try {
                        connectionManager.bindProcessToNetwork(network)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

            val fragment = DeviceViewFragment.newInstance(DeviceItem(DeviceDiscovery.DEFAULT_WLED_AP_IP))
            switchContent(R.id.fragment_container_view, fragment)
        }
    }
    companion object {
        private val TAG = MainActivity::class.qualifiedName
    }

    override fun onBackStackChanged() {
        updateIsBackArrowVisible()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            //Title bar back press triggers onBackPressed()
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0 ) {

            val deviceViewFragment: DeviceViewFragment? = supportFragmentManager.findFragmentByTag(DeviceViewFragment.TAG_NAME) as DeviceViewFragment?
            if (deviceViewFragment != null && deviceViewFragment.isVisible) {
                if (deviceViewFragment.onBackPressed()) {
                    return
                }
            }

            supportFragmentManager.popBackStack()
        }
        else {
            super.onBackPressed()
        }
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
    }

    private fun updateIsBackArrowVisible() {
        supportActionBar?.setDisplayHomeAsUpEnabled(
            supportFragmentManager.backStackEntryCount > 0
        )
    }
}