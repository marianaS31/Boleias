package pt.ipp.estg.cmugrupo8.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val image: ByteArray?,
    val drivingLicense: String ?,
    val isActive: Boolean = true,
    val isAdmin: Boolean = false
)