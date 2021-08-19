package ca.cgagnier.wlednativeandroid.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileSystem (

	@Json(name = "u") val spaceUsed : Int,
	@Json(name = "t") val spaceTotal : Int,
	@Json(name = "pmt") val presetLastModification : Int
)