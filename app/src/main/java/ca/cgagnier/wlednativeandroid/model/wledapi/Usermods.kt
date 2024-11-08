package ca.cgagnier.wlednativeandroid.model.wledapi

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonQualifier


@JsonClass(generateAdapter = true)
data class Usermods (

	@Json(name = "Battery level") val batLevel : List<Any>? = null,
)