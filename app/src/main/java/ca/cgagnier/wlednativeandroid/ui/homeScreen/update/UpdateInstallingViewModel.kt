package ca.cgagnier.wlednativeandroid.ui.homeScreen.update

import androidx.lifecycle.ViewModel
import ca.cgagnier.wlednativeandroid.model.Device
import ca.cgagnier.wlednativeandroid.model.VersionWithAssets
import ca.cgagnier.wlednativeandroid.repository.VersionWithAssetsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class UpdateInstallingViewModel @Inject constructor(
    versionWithAssetsRepository: VersionWithAssetsRepository
): ViewModel() {
    private var _state = MutableStateFlow(UpdateInstallingState())
    val state = _state.asStateFlow()

    private var _device: MutableStateFlow<Device?> = MutableStateFlow(null)
    val device = _device.asStateFlow()

    private var _version: MutableStateFlow<VersionWithAssets?> = MutableStateFlow(null)
    val version = _version.asStateFlow()

    fun startUpdate(device: Device, version: VersionWithAssets) {
        _device.update { device }
        _version.update { version }
    }
}