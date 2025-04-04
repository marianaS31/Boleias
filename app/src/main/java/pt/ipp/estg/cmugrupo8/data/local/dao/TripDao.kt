package pt.ipp.estg.cmugrupo8.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.ipp.estg.cmugrupo8.data.local.entities.*
import pt.ipp.estg.cmugrupo8.data.local.models.*

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip)

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT * FROM Trip WHERE id = :tripId")
    suspend fun getTripById(tripId: Int): Trip?

    @Query("SELECT * FROM Trip")
    fun getAllTrips(): LiveData <List<Trip>>

    @Query("""
        SELECT Trip.*, Location.id AS locationId, Location.tripId AS locationTripId, 
               Location.latitude, Location.longitude
        FROM Trip
        LEFT JOIN Location ON Trip.id = Location.tripId
        WHERE Trip.id = :tripId
    """)
    fun getTripWithLocations(tripId: Int): LiveData <TripLocations>

    @Query("""
        SELECT Trip.*, Classification.id AS classificationId, Classification.tripId AS classificationTripId, 
               Classification.userId AS classificationUserId, Classification.driverClassification, Classification.userClassification
        FROM Trip
        LEFT JOIN Classification ON Trip.id = Classification.tripId
        WHERE Trip.id = :tripId
    """)
    fun getTripWithClassifications(tripId: Int): LiveData <TripClassifications>

    @Query("""
        SELECT Trip.*, Payment.id AS paymentId, Payment.tripId AS paymentTripId,
               Payment.paymentMethod AS paymentMethod, Payment.userId AS paymentUserId
        FROM Trip
        LEFT JOIN Payment ON Trip.id = Payment.tripId
        WHERE Trip.id = :tripId
    """)
    fun getTripWithPayments(tripId: Int): LiveData <TripPayments>
}
