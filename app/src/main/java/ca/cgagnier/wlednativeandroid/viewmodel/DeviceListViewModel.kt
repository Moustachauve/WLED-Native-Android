package ca.cgagnier.wlednativeandroid.viewmodel

import androidx.lifecycle.ViewModel
import ca.cgagnier.wlednativeandroid.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
    private val repository: DeviceRepository,
    //userPreferencesRepository: UserPreferencesRepository
): ViewModel() {
/*
    @OptIn(ExperimentalCoroutinesApi::class)
    val allDevices: LiveData<List<Device>> = userPreferencesRepository.showOfflineDevicesLast.flatMapLatest { showOfflineLast ->
        if (showOfflineLast) {
            repository.allVisibleDevicesOfflineLast
        } else {
            repository.allVisibleDevices
        }
    }.asLiveData()
*/
    val allDevicesFlow = repository.allVisibleDevicesOfflineLast

    companion object {
        const val TAG = "DeviceListViewModel"
    }
}