package ca.cgagnier.wlednativeandroid.service.device.api.request

import ca.cgagnier.wlednativeandroid.model.StatefulDevice

class RefreshRequest(
    device: StatefulDevice,
    val silentRefresh: Boolean = false,
    val saveChanges: Boolean = true,
    val callback: (suspend (StatefulDevice) -> Unit)? = null
) : Request(device)