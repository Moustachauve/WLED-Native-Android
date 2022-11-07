package ca.cgagnier.wlednativeandroid.model.wledapi

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Nightlight (

	@Json(name = "on") val isOn : Boolean? = null,
	@Json(name = "dur") val duration : Int? = null,
	@Json(name = "fade") val fade : Boolean? = null,
	@Json(name = "mode") val mode : Int? = null,
	@Json(name = "tbri") val targetBrightness : Int? = null,
	@Json(name = "rem") val remainingTime : Int? = null
)