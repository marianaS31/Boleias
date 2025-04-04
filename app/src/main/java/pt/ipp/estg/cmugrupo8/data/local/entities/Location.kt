package pt.ipp.estg.cmugrupo8.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "Location",
    foreignKeys = [ForeignKey(entity = Trip::class, parentColumns = ["id"], childColumns = ["tripId"],onDelete = ForeignKey.CASCADE)]
)
data class Location(
    @PrimaryKey (autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val tripId: Int
)
