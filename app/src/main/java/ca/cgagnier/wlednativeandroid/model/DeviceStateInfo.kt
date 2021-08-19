package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceStateInfo (

	@Json(name = "state") val state : State,
	@Json(name = "info") val info : Info
)