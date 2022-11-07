package ca.cgagnier.wlednativeandroid

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Options(
    @Json(name = "version")
    val version: Int,
    @Json(name = "lastSelectedAddress")
    val lastSelectedAddress: String
)