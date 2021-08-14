package ca.cgagnier.wlednativeandroid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import ca.cgagnier.wlednativeandroid.fragment.DeviceListFragment
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository


class MainActivity : AppCompatActivity(R.layout.activity_main), FragmentManager.OnBackStackChangedListener {

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
            supportFragmentManager.popBackStack()
        }
        else {
            super.onBackPressed()
        }
    }

    fun switchContent(id: Int, fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(id, fragment, fragment.toString())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}