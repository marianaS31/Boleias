package pt.ipp.estg.cmugrupo8.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import pt.ipp.estg.cmugrupo8.data.local.dao.UserDao

@Entity(
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"])]
)
data class Vehicle(
    @PrimaryKey val plateNumber: String,
    val brand: String,
    val model: String,
    val color: String,
    val year: String,
    val seats: Int,
    val userId: Int
)
