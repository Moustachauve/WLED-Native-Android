package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Nightlight (

	@Json(name = "on") val isOn : Boolean,
	@Json(name = "dur") val duration : Int,
	@Json(name = "fade") val fade : Boolean,
	@Json(name = "mode") val mode : Int,
	@Json(name = "tbri") val targetBrightness : Int,
	@Json(name = "rem") val remainingTime : Int
)