package ca.cgagnier.wlednativeandroid.ui.homeScreen.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UpdateDetailsViewModel @Inject constructor(
    versionWithAssetsRepository: VersionWithAssetsRepository
): ViewModel() {

    private var _device = MutableStateFlow<Device?>(null)
    val device = _device.asStateFlow()

    private var versionDetails = device.map { device ->
        if (device == null) {
            return@map null
        }
        val version = device.newUpdateVersionTagAvailable
        versionWithAssetsRepository.getVersionByTag(version)
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        initialValue = null
    )

    val state = combine(device, versionDetails) { device, versionDetails ->
        if (device == null) {
            UpdateDetailsState()
        } else {
            UpdateDetailsState(
                version = versionDetails?.version
            )
        }
    }.stateIn(
        viewModelScope,
        WhileSubscribed(5000),
        initialValue = UpdateDetailsState()
    )

    fun setDevice(device: Device) {
        _device.value = device
    }
}
