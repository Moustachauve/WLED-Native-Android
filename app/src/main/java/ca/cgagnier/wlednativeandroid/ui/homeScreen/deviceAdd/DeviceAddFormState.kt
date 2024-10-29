package ca.cgagnier.wlednativeandroid.ui.homeScreen.deviceAdd

import androidx.annotation.StringRes

data class DeviceAddFormState(
    val address: String = "",
    @StringRes val addressError: Int? = null,
    val name: String = "",
    val isHidden: Boolean = false,
)