package ca.cgagnier.wlednativeandroid.model.wledapi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonPost (
	@Json(name = "on") val isOn : Boolean? = null,
	@Json(name = "bri") val brightness : Int? = null,


	// "v" will make the post request return the current state of the device
	// So we can also update the UI while setting values
	@Json(name = "v") val verbose : Boolean = true
)