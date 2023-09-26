package ca.cgagnier.wlednativeandroid.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.net.URL
import java.util.Date

@Entity
data class Version(
    @PrimaryKey
    val tagName: String,
    val name: String,
    val description: String,
    val isPrerelease: Boolean,
    val publishedDate: String,
    val htmlUrl: String,
)
