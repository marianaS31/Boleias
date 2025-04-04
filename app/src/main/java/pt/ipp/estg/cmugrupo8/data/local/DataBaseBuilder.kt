    package pt.ipp.estg.cmugrupo8.data.local

    import android.content.Context
    import androidx.compose.foundation.layout.Column
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.room.Database
    import androidx.room.Room
    import androidx.room.RoomDatabase
    import pt.ipp.estg.cmugrupo8.data.local.dao.ClassificationDao
    import pt.ipp.estg.cmugrupo8.data.local.dao.LocationDao
    import pt.ipp.estg.cmugrupo8.data.local.dao.PaymentDao
    import pt.ipp.estg.cmugrupo8.data.local.dao.TripDao
    import pt.ipp.estg.cmugrupo8.data.local.dao.UserDao
    import pt.ipp.estg.cmugrupo8.data.local.dao.UserTripDao
    import pt.ipp.estg.cmugrupo8.data.local.dao.VehicleDao
    import pt.ipp.estg.cmugrupo8.data.local.entities.Classification
    import pt.ipp.estg.cmugrupo8.data.local.entities.Location
    import pt.ipp.estg.cmugrupo8.data.local.entities.Payment
    import pt.ipp.estg.cmugrupo8.data.local.entities.Trip
    import pt.ipp.estg.cmugrupo8.data.local.entities.User
    import pt.ipp.estg.cmugrupo8.data.local.entities.UserTrip
    import pt.ipp.estg.cmugrupo8.data.local.entities.Vehicle

    @Database(
        entities = [
            User::class,
            Vehicle::class,
            Trip::class,
            Location::class,
            Classification::class,
            Payment::class,
            UserTrip::class
        ],
        version = 3,
    )

    abstract class AppDatabase : RoomDatabase() {

        abstract fun userDao(): UserDao
        abstract fun vehicleDao(): VehicleDao
        abstract fun tripDao(): TripDao
        abstract fun locationDao(): LocationDao
        abstract fun classificationDao(): ClassificationDao
        abstract fun paymentDao(): PaymentDao
        abstract fun userTripDao(): UserTripDao

        companion object{
            private var INSTANCE:AppDatabase?=null

            fun getDatabase(context: Context):AppDatabase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context,
                        AppDatabase::class.java,
                        "app-database"
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    instance
                }
            }
        }
    }
