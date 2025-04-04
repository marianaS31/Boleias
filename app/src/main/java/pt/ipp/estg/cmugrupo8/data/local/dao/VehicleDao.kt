package pt.ipp.estg.cmugrupo8.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*

@Dao
interface VehicleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vehicle: Vehicle)

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)

    @Query("SELECT * FROM Vehicle WHERE plateNumber = :plateNumber")
    suspend fun getVehicleByPlateNumber(plateNumber: String): Vehicle?

    @Query("Select * FROM Vehicle WHERE userId = :userId")
    suspend fun getVehicleByUserId(userId: Int): Vehicle?

    @Query("SELECT * FROM Vehicle")
    fun getAllVehicles(): LiveData <List<Vehicle>>
}