package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DeviceViewViewModel : ViewModel() {
    var currentUrl: String = ""
    val backQueue = ArrayDeque<String>(5)
    var isGoingBack = false
    var loadingCounter = 0
    var webAlreadyLoaded = false
}


class DeviceViewViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceViewViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
