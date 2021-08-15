package ca.cgagnier.wlednativeandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.*
import ca.cgagnier.wlednativeandroid.fragment.DeviceAddManuallyFragment
import ca.cgagnier.wlednativeandroid.fragment.DeviceListFragment
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import ca.cgagnier.wlednativeandroid.fragment.DeviceViewFragment


class MainActivity : AppCompatActivity(R.layout.activity_main),
    FragmentManager.OnBackStackChangedListener,
    DeviceAddManuallyFragment.NoticeDialogListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DeviceRepository.init(applicationContext)

        setSupportActionBar(findViewById(R.id.main_toolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(R.drawable.wled_logo_akemi)

        supportFragmentManager.addOnBackStackChangedListener(this)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<DeviceListFragment>(R.id.fragment_container_view)
            }
        }
    }

    override fun onBackStackChanged() {
        supportActionBar?.setDisplayHomeAsUpEnabled(
            supportFragmentManager.backStackEntryCount > 0
        )
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
}