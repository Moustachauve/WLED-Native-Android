package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class State (

	@Json(name = "on") val isOn : Boolean,
	@Json(name = "bri") val brightness : Int,
	@Json(name = "transition") val transition : Int,
	@Json(name = "ps") val selectedPresetId : Int,
	@Json(name = "pl") val selectedPlaylistId : Int,
	@Json(name = "nl") val nightlight : Nightlight,
	@Json(name = "lor") val liveDataOverride : Int,
	@Json(name = "mainseg") val mainSegment : Int,
	@Json(name = "seg") val segment : List<Segment>
)