package ca.cgagnier.wlednativeandroid.service.device

import ca.cgagnier.wlednativeandroid.AppContainer
import ca.cgagnier.wlednativeandroid.model.Device

class StateFactory(private val appContainer: AppContainer) {

    private var allStates = mutableMapOf<String, State>()

    fun getState(device: Device): State {
        if (!allStates.contains(device.address)) {
            allStates[device.address] = State(device, appContainer)
        }

        return allStates[device.address]!!
    }
}