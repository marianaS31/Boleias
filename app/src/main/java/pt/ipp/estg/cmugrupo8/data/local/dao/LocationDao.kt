package pt.ipp.estg.cmugrupo8.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: Location)

    @Update
    suspend fun update(location: Location)

    @Delete
    suspend fun delete(location: Location)

    @Query("SELECT * FROM Location WHERE id = :locationId")
    suspend fun getLocationById(locationId: Int): Location?

    @Query("SELECT * FROM Location WHERE tripId = :tripId")
    fun getLocationsByTripId(tripId: Int): LiveData <List<Location>>

    @Query("SELECT * FROM Location")
    fun getAllLocations(): LiveData <List<Location>>
}
