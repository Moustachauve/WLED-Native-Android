package ca.cgagnier.wlednativeandroid.service.device

import ca.cgagnier.wlednativeandroid.DevicesApplication
import ca.cgagnier.wlednativeandroid.model.Device

class StateFactory(val application: DevicesApplication) {

    private var allStates = mutableMapOf<String, State>()

    fun getState(device: Device): State {
        if (!allStates.contains(device.address)) {
            allStates[device.address] = State(device, application)
        }

        return allStates[device.address]!!
    }
}