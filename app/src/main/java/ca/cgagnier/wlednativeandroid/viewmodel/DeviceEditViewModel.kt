package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.cgagnier.wlednativeandroid.R
import ca.cgagnier.wlednativeandroid.model.Branch
import ca.cgagnier.wlednativeandroid.model.Device

class DeviceEditViewModel: ViewModel() {
    lateinit var device: Device

    var customName = ""
    var hideDevice = false
    var updateBranch = Branch.UNKNOWN

    var updateCheckStartTime: Long = 0

    fun isDeviceSet(): Boolean {
        return ::device.isInitialized
    }

    fun setDeviceVariables(device: Device) {
        this.device = device
        customName = if (device.isCustomName) device.name else ""
        hideDevice = device.isHidden
        updateBranch = device.branch
    }

    fun getViewIdForCurrentBranch(): Int {
        return when (updateBranch) {
            Branch.BETA -> R.id.branch_beta_button
            else -> R.id.branch_stable_button
        }
    }

    fun getBranchFromViewId(viewId: Int): Branch {
        return when (viewId) {
            R.id.branch_beta_button -> Branch.BETA
            else -> Branch.STABLE
        }
    }

    fun branchHasChanged(): Boolean {
        return device.branch != updateBranch
    }

    fun isCustomName(): Boolean {
        return customName != ""
    }
}

class DeviceEditViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceEditViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
