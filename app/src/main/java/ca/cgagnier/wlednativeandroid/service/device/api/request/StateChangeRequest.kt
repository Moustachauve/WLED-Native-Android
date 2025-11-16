package ca.cgagnier.wlednativeandroid.service.device.api.request

import ca.cgagnier.wlednativeandroid.model.StatefulDevice
import ca.cgagnier.wlednativeandroid.model.wledapi.JsonPost

class StateChangeRequest(
    device: StatefulDevice, val state: JsonPost, val saveChanges: Boolean = true
) : Request(device)